package com.teamface.custom.service.module;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.common.CommonAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年12月7日 下午4:08:32
 * @version: 1.0
 */
@Service("moduleSingleShareSettingAppService")
public class ModuleSingleShareSettingAppServiceImpl implements ModuleSingleShareSettingAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private CommonAppService commonAppService;
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    protected RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    @Override
    public ServiceResult<String> saveSetting(Map map)
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
        String[] dataIds = fieldJson.getString("dataId").split(",");
        for (String dataId : dataIds)
        {
            
            String beanName = fieldJson.getString("bean_name");
            // 获取新增数据id
            JSONObject basic = fieldJson.getJSONObject("basics");
            basic.put("module_id", dataId);
            basic.put("bean_name", beanName);
            basic.put("employee_id", employeeId);
            basic.put(Constant.FIELD_DEL_STATUS, 0);
            basic.put(Constant.FIELD_CREATE_BY, employeeId);
            basic.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
            // 保存数据共享设置
            JSONObject json = new JSONObject();
            json.put("data", basic.toString());
            json.put("bean", "module_data_share_setting");
            String insertSql = JSONParser4SQL.getInsertSql(json, companyId.toString());
            int count = DAOUtil.executeUpdate(insertSql);
            if (count < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            }
            
            // 添加动态
            Map<String, Object> resultMap = new HashMap<String, Object>();
            resultMap.put("relation_id", dataId);
            resultMap.put("datetime_time", System.currentTimeMillis());
            StringBuilder information = new StringBuilder();
            information.append(" 将数据共享给【").append(basic.getString("target_lable")).append("】");
            resultMap.put("content", information.toString());
            resultMap.put("bean", beanName);
            resultMap.put("employee_id", employeeId);
            resultMap.put("company_id", companyId);
            boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
            if (!operationRecord)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            // 助手推送
            String parameters = "{'company_id':'" + companyId + "','push_type':'2','id':'" + dataId + "','bean_name':'" + beanName + "'}";
            rabbitMQServer.sendMessage(Constant.PUSH_THREAD_QUEUE_NAME, parameters);
        }
        return serviceResult;
        
    }
    
    @Override
    public JSONObject getSettingById(Map map)
    {
        if (StringUtils.isEmpty(map.get("shareId")))
        {
            return null;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        // 根据菜单栏获取模块ID
        String table = DAOUtil.getTableName("module_data_share_setting", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append("select * from ")
            .append(table)
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and id=")
            .append(map.get("shareId"))
            .append(" and employee_id=")
            .append(employeeId);
        return DAOUtil.executeQuery4FirstJSON(builder.toString());
        
    }
    
    @Override
    public List<JSONObject> getSettings(Map map)
    {
        if (StringUtils.isEmpty(map.get("dataId")))
        {
            return null;
        }
        // 解析tonken
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String table = DAOUtil.getTableName("module_data_share_setting", companyId);
        StringBuilder sql = new StringBuilder();
        sql.append("select * from ")
            .append(table)
            .append(" where ")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 and module_id=")
            .append(map.get("dataId"))
            .append(" and employee_id=")
            .append(employeeId)
            .append(" order by datetime_create_time ");
        return DAOUtil.executeQuery4JSON(sql.toString());
        
    }
    
    @Override
    public ServiceResult<String> delSetting(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        if (StringUtils.isEmpty(map.get("id")))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String beanName = "module_data_share_setting";
        String table = DAOUtil.getTableName(beanName, companyId);
        
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        StringBuilder sql = new StringBuilder();
        sql.setLength(0);
        sql.append(" select * from ").append(table).append(" where id=").append(map.get("id")).append(" and ").append(Constant.FIELD_DEL_STATUS).append("=").append("0");
        JSONObject obj = DAOUtil.executeQuery4FirstJSON(sql.toString());
        if (obj != null)
        {
            sql.setLength(0);
            sql.append(" select * from ")
                .append(DAOUtil.getTableName("operation_record", companyId))
                .append(" where relation_id=")
                .append(obj.getString("module_id"))
                .append(" and employee_id=")
                .append(employeeId);
            JSONObject module = DAOUtil.executeQuery4FirstJSON(sql.toString());
            if (module != null)
            {
                
                // 添加动态
                Map<String, Object> resultMap = new HashMap<String, Object>();
                resultMap.put("relation_id", obj.getString("module_id"));
                resultMap.put("datetime_time", System.currentTimeMillis());
                resultMap.put("content", "个人共享删除");
                resultMap.put("bean", module.getString("bean"));
                resultMap.put("employee_id", employeeId);
                resultMap.put("company_id", companyId);
                boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
                if (!operationRecord)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        sql.setLength(0);
        sql.append("update ").append(table).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id=").append(map.get("id"));
        DAOUtil.executeUpdate(sql.toString());
        return serviceResult;
        
    }
    
    @Override
    public ServiceResult<String> updateSetting(Map map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        JSONObject fieldJson = JSONObject.parseObject((String)map.get("data"));
        String shareId = fieldJson.getString("id");
        if (StringUtils.isEmpty(shareId))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String beanName = fieldJson.getString("bean_name");
        // 验证权限
        serviceResult = moduleDataAuthAppService.checkOperateAuth(token, beanName, Constant.AUTH_SHARE);
        if (!serviceResult.isSucces())
        {
            return serviceResult;
        }
        
        JSONObject basic = fieldJson.getJSONObject("basics");
        basic.put(Constant.FIELD_DEL_STATUS, 0);
        basic.put("module_id", fieldJson.getString("dataId"));
        basic.put("bean_name", beanName);
        basic.put("employee_id", employeeId);
        basic.put(Constant.FIELD_DEL_STATUS, 0);
        basic.put(Constant.FIELD_CREATE_BY, employeeId);
        basic.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        // 保存数据共享设置
        JSONObject json = new JSONObject();
        json.put("id", shareId);
        json.put("data", basic.toString());
        json.put("bean", "module_data_share_setting");
        String insertSql = JSONParser4SQL.getUpdateSql(json, companyId.toString());
        int count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        
        // 添加动态
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("relation_id", fieldJson.getString("dataId"));
        resultMap.put("datetime_time", System.currentTimeMillis());
        resultMap.put("content", "个人共享修改");
        resultMap.put("bean", beanName);
        resultMap.put("employee_id", employeeId);
        resultMap.put("company_id", companyId);
        boolean operationRecord = commonAppService.savaOperationRecord(resultMap);
        if (!operationRecord)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        
        return serviceResult;
        
    }
    
}
