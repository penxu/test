package com.teamface.custom.service.module;

import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年1月20日 下午5:23:52
 * @version: 1.0
 */
@Service("moduleTeapoolSettingAppService")
public class ModuleTeapoolSettingAppServiceImpl implements ModuleTeapoolSettingAppService
{
    
    private static final Logger LOG = LogManager.getLogger(ModuleTeapoolSettingAppService.class);
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Override
    public ServiceResult<String> saveTeapool(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        String token = map.get("token").toString();
        if (StringUtils.isEmpty(map.get("data")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        JSONObject fieldJson = JSONObject.parseObject((String)map.get("data"));
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, fieldJson.getString("bean_name"), Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        StringBuilder sql = new StringBuilder();
        sql.setLength(0);
        sql.append(" select id from ").append(DAOUtil.getTableName("application_module", companyId)).append(" where english_name='").append(fieldJson.get("bean_name")).append("'");
        JSONObject first = DAOUtil.executeQuery4FirstJSON(sql.toString());
        sql.setLength(0);
        sql.append("select count(1) from ")
            .append(DAOUtil.getTableName("module_seapool_setting", companyId))
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append(" = 0 and title='")
            .append(fieldJson.get("title"))
            .append("'");
        int count = DAOUtil.executeCount(sql.toString());
        if (count > 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.seapool.name.is.exist"), resultCode.getMsgZh("common.seapool.name.is.exist"));
            return serviceResult;
        }
        // 获取新增数据id
        fieldJson.put("module_id", first.get("id"));
        fieldJson.put(Constant.FIELD_DEL_STATUS, 0);
        fieldJson.put(Constant.FIELD_CREATE_BY, employeeId);
        fieldJson.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        // 保存数据共享设置
        JSONObject json = new JSONObject();
        json.put("data", fieldJson.toString());
        json.put("bean", "module_seapool_setting");
        String insertSql = JSONParser4SQL.getInsertSql(json, companyId.toString());
        count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
        
    }
    
    @Override
    public JSONObject getTeapoolById(Map map)
    {
        if (StringUtils.isEmpty(map.get("seapoolId")))
        {
            return null;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        // 根据菜单栏获取模块ID
        String table = DAOUtil.getTableName("module_seapool_setting", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select * from ").append(table).append(" where ").append(Constant.FIELD_DEL_STATUS).append("=0 and id=").append(map.get("seapoolId"));
        return DAOUtil.executeQuery4FirstJSON(builder.toString());
        
    }
    
    @Override
    public List<JSONObject> getTeapools(Map map)
    {
        if (StringUtils.isEmpty(map.get("bean")))
        {
            return null;
        }
        // 解析tonken
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        LOG.debug(String.format(" getTeapools parameters{args0:%s,args1:%s} start!", companyId, map.get("bean").toString()));
        String table = DAOUtil.getTableName("module_seapool_setting", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select id, title, employee_target_lable from ")
            .append(table)
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and bean_name='")
            .append(map.get("bean"))
            .append("'")
            .append(" order by datetime_create_time ");
        return DAOUtil.executeQuery4JSON(sql.toString());
        
    }
    
    @Override
    public ServiceResult<String> delTeapool(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("id")) || StringUtils.isEmpty(map.get("bean")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String beanName = "module_seapool_setting";
        String table = DAOUtil.getTableName(beanName, companyId);
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_DEL);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        StringBuilder sql = new StringBuilder();
        // 验证是否存在引用公海池的数据
        sql.append(" select count(1) from ").append(DAOUtil.getTableName(map.get("bean").toString(), companyId)).append(" where del_status=0 and seas_pool_id=").append(map.get("id"));
        int exist = DAOUtil.executeCount(sql.toString());
        if (exist > 0)
        {
            serviceResult.setCodeMsg("1000", " 删除公海池失败，请先删除/转移当前公海池内的数据。");
            return serviceResult;
        }
        sql.setLength(0);
        sql.append("update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id=").append(map.get("id"));
        DAOUtil.executeUpdate(sql.toString());
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> updateTeapool(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject fieldJson = JSONObject.parseObject((String)map.get("data"));
        String seaPoolId = fieldJson.getString("id");
        fieldJson.remove("id");
        if (StringUtils.isEmpty(seaPoolId))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, fieldJson.getString("bean_name"), Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        StringBuilder sql = new StringBuilder();
        sql.setLength(0);
        sql.append("select count(1) from ")
            .append(DAOUtil.getTableName("module_seapool_setting", companyId))
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append(" = 0 and title='")
            .append(fieldJson.get("title"))
            .append("'")
            .append(" and id !=")
            .append(seaPoolId);
        int count = DAOUtil.executeCount(sql.toString());
        if (count > 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.seapool.name.is.exist"), resultCode.getMsgZh("common.seapool.name.is.exist"));
            return serviceResult;
        }
        fieldJson.put(Constant.FIELD_DEL_STATUS, 0);
        fieldJson.put(Constant.FIELD_CREATE_BY, employeeId);
        fieldJson.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        // 保存数据共享设置
        JSONObject json = new JSONObject();
        json.put("id", seaPoolId);
        json.put("data", fieldJson.toString());
        json.put("bean", "module_seapool_setting");
        String insertSql = JSONParser4SQL.getUpdateSql(json, companyId.toString());
        count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        return serviceResult;
        
    }
    
}
