package com.teamface.custom.service.transformation;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月29日 下午3:45:38
 * @version: 1.0
 */
@Service("fieldTransformationAppService")
public class FieldTransformationAppServiceImpl implements FieldTransformationAppService
{
    
    private static final Logger log = LogManager.getLogger(FieldTransformationAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Override
    public List<JSONObject> getFieldTransformationsForMobile(Map map)
        throws Exception
    {
        log.debug(" getFieldTransformationsForMobile start !");
        String token = map.get("token").toString();
        String bean = map.get("bean").toString();
        if (StringUtils.isEmpty(bean))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 验证权限
        ServiceResult<String> serviceResult = moduleDataAuthAppService.checkOperateAuth(token, bean, Constant.AUTH_TRANSFER);
        if (!serviceResult.isSucces())
        {
            return null;
        }
        // 查询条件
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("bean", bean);
        return LayoutUtil.findModuleFieldTransferSetLayout(queryDoc, Constant.FIELD_COLLECTION);
        
    }
    
    @Override
    public List<JSONObject> getFieldTransformationsForPc(Map map)
        throws Exception
    {
        String token = map.get("token").toString();
        String bean = map.get("bean").toString();
        if (StringUtils.isEmpty(bean))
        {
            return null;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 验证权限
        ServiceResult<String> serviceResult = moduleDataAuthAppService.checkOperateAuth(token, bean, Constant.AUTH_TRANSFER);
        if (!serviceResult.isSucces())
        {
            return null;
        }
        // 查询条件
        Document queryDoc = new Document();
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("bean", bean);
        return LayoutUtil.findModuleSetLayout(queryDoc, Constant.FIELD_COLLECTION);
        
    }
    
    @Override
    public JSONObject getFieldTransformationById(Map map)
        throws Exception
    {
        String id = map.get("id").toString();
        if (StringUtils.isEmpty(id))
        {
            return null;
        }
        JSONObject fieldLayout = LayoutUtil.getById(Constant.FIELD_COLLECTION, id);
        fieldLayout.remove("_id");
        fieldLayout.put("id", id);
        return fieldLayout;
        
    }
    
    @Override
    public ServiceResult<String> saveFieldTransformation(Map map)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        String token = map.get("token").toString();
        String data = map.get("data").toString();
        if (StringUtils.isEmpty(data))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject fieldJson = JSONObject.parseObject(data);
        fieldJson.put("companyId", companyId.toString());
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, fieldJson.getString("bean"), Constant.AUTH_TRANSFER);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        boolean flag = LayoutUtil.saveModuleSetLayout(fieldJson, Constant.FIELD_COLLECTION);
        if (flag)
        {
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        }
        else
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.transform.name.is.exist"));
        }
        
        return serviceResult;
    }
    
    @Override
    public ServiceResult<String> delFieldTransformation(Map map)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        JSONObject fieldJson = JSONObject.parseObject((String)map.get("data"));
        String id = fieldJson.getString("id");
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        fieldJson = LayoutUtil.getById(Constant.FIELD_COLLECTION, id);
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, fieldJson.getString("bean"), Constant.AUTH_TRANSFER);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        LayoutUtil.removeModuleSetLayout(id, Constant.FIELD_COLLECTION);
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> updateFieldTransformation(Map map)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        JSONObject fieldJson = JSONObject.parseObject((String)map.get("data"));
        fieldJson.put("companyId", companyId.toString());
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, fieldJson.getString("bean"), Constant.AUTH_TRANSFER);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        boolean flag = LayoutUtil.modifyModuleSetLayout(fieldJson, Constant.FIELD_COLLECTION);
        if (flag)
        {
            serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        }
        else
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.transform.name.is.exist"));
        }
        
        return serviceResult;
    }
    
}
