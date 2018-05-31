package com.teamface.custom.service.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.application.ApplicationModuleUsedAppService;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.library.FileLibraryAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月23日 上午11:33:55
 * @version: 1.0
 */
@Service("moduleAppService")
public class ModuleAppServiceImpl implements ModuleAppService
{
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(ModuleAppServiceImpl.class);
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    private ApplicationModuleUsedAppService applicationModuleUsedAppService;
    
    @Autowired
    FileLibraryAppService fileLibraryAppService;
    
    @Override
    public String getModulesByApplication(Map map)
    {
        if (StringUtils.isEmpty(map.get("token")) || StringUtils.isEmpty(map.get("applicationId")))
        {
            return null;
        }
        // 解析token
        String token = map.get("token").toString();
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        String applicationTable = DAOUtil.getTableName("application", companyId);
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        StringBuilder querySql = new StringBuilder();
        querySql.append(" select t.*, (select  name from ").append(applicationTable).append(" where id = t.application_id ) as application_name from ").append(moduleTable).append(
            " t where id in (");
        querySql.append("select t2.id from ");
        querySql.append(roleModuleTable);
        querySql.append(" t1, ");
        querySql.append(moduleTable);
        querySql.append(" t2 where t1.module_id = t2.id and t1.role_id = ");
        querySql.append(roleId);
        querySql.append(" and t2.").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        // 0：pc、手机都不显示 1:手机 2：pc 3：都显示
        querySql.append(" and ( t2.terminal_app=2 or t2.terminal_app=3 ) ");
        querySql.append(" and t2.application_id = ");
        querySql.append(map.get("applicationId"));
        querySql.append(")");
        List<JSONObject> modules = DAOUtil.executeQuery4JSON(querySql.toString());
        querySql.setLength(0);
        int i = 0;
        for (JSONObject m : modules)
        {
            if (i > 0)
            {
                querySql.append(",");
            }
            i++;
            querySql.append(m.get("id"));
        }
        return querySql.toString();
    }
    
    @Override
    public String getModuleBelongWhichApp(Map map)
    {
        if (StringUtils.isEmpty(map.get("token")) || StringUtils.isEmpty(map.get("bean")))
        {
            return null;
        }
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("application_module", companyId);
        StringBuilder builder = new StringBuilder();
        builder.append(" select application_id from ").append(table).append(" where english_name='").append(map.get("bean")).append("'");
        JSONObject app = DAOUtil.executeQuery4FirstJSON(builder.toString());
        if (app != null)
        {
            return app.getString("application_id");
        }
        return null;
    }
    
    /**
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     * @Description:保存模块数据
     */
    @Override
    public ServiceResult<String> saveModule(String token, String reqJsonStr, Long moduleId)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        // 请求参数
        JSONObject layoutJson = JSONObject.parseObject(reqJsonStr);
        String beanName = layoutJson.getString("bean");
        String titleName = layoutJson.getString("title");
        String applicationId = layoutJson.getString("applicationId");
        String icon = layoutJson.getString("icon");
        String edition = layoutJson.getString("edition");
        String icon_type = layoutJson.getString("icon_type");
        String icon_url = layoutJson.getString("icon_url");
        String icon_color = layoutJson.getString("icon_color");
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        
        String bean = "application_module";
        StringBuilder getTopperSql = new StringBuilder();
        getTopperSql.append(" select count(1) from ")
            .append(DAOUtil.getTableName(bean, companyId))
            .append(" where application_id = ")
            .append(applicationId)
            .append(" and ")
            .append(Constant.FIELD_DEL_STATUS)
            .append(" = 0 limit 1 offset 0");
        
        int topper = DAOUtil.executeCount(getTopperSql.toString());
        topper++;
        // 保存模块数据
        JSONObject moduleObj = new JSONObject();
        // 0：pc、手机都不显示 1:手机 2：pc 3：都显示
        if ("0".equals(layoutJson.getString("terminalApp")) && "0".equals(layoutJson.getString("terminalPc")))
        {
            moduleObj.put("terminal_app", "0");
        }
        else if ("1".equals(layoutJson.getString("terminalApp")) && "1".equals(layoutJson.getString("terminalPc")))
        {
            moduleObj.put("terminal_app", "3");
        }
        else if ("0".equals(layoutJson.getString("terminalApp")) && "1".equals(layoutJson.getString("terminalPc")))
        {
            moduleObj.put("terminal_app", "2");
        }
        else if ("1".equals(layoutJson.getString("terminalApp")) && "0".equals(layoutJson.getString("terminalPc")))
        {
            moduleObj.put("terminal_app", "1");
        }
        
        moduleObj.put("application_id", applicationId);
        moduleObj.put("chinese_name", titleName);
        moduleObj.put("english_name", beanName);
        moduleObj.put("icon", icon);
        moduleObj.put("icon_type", icon_type);
        moduleObj.put("icon_color", icon_color);
        moduleObj.put("icon_url", icon_url);
        moduleObj.put("edition", edition);
        moduleObj.put("id", moduleId);
        moduleObj.put(Constant.FIELD_DEL_STATUS, 0);
        moduleObj.put("topper", topper);
        moduleObj.put(Constant.FIELD_CREATE_BY, employeeId);
        moduleObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
        JSONObject module = new JSONObject();
        module.put("data", moduleObj.toString());
        module.put("bean", bean);
        String insertSql = JSONParser4SQL.getInsertSql(module, companyId.toString());
        int count = DAOUtil.executeUpdate(insertSql);
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        else
        {
            LOG.error("时间" + System.currentTimeMillis() + " 名称 " + moduleObj.get("chinese_name"));
            // 保存文件夹
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("type", "1");
            jsonObject.put("chinese_name", moduleObj.get("chinese_name"));
            jsonObject.put("english_name", moduleObj.get("english_name"));
            jsonObject.put("id", moduleId);
            jsonObject.put("parent_id", applicationId);
            fileLibraryAppService.savaLibrary(token, jsonObject);
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @param bean
     * @return ServiceResult
     * @Description:根据bean修改模块信息
     */
    @Override
    public ServiceResult<String> modifyModuleByBean(String token, String bean, JSONObject json)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String moduleTable = DAOUtil.getTableName("application_module", companyId.toString());
        String terminal_app = "0";
        // 0：pc、手机都不显示 1:手机 2：pc 3：都显示
        if ("0".equals(json.getString("terminalApp")) && "0".equals(json.getString("terminalPc")))
        {
            terminal_app = "0";
        }
        else if ("1".equals(json.getString("terminalApp")) && "1".equals(json.getString("terminalPc")))
        {
            terminal_app = "3";
        }
        else if ("0".equals(json.getString("terminalApp")) && "1".equals(json.getString("terminalPc")))
        {
            terminal_app = "2";
        }
        else if ("1".equals(json.getString("terminalApp")) && "0".equals(json.getString("terminalPc")))
        {
            terminal_app = "1";
        }
        StringBuilder modifySql = new StringBuilder();
        modifySql.append("update ");
        modifySql.append(moduleTable);
        modifySql.append(" set chinese_name = '");
        modifySql.append(json.getString("title"));
        modifySql.append("', terminal_app='");
        modifySql.append(terminal_app);
        modifySql.append("', icon='");
        modifySql.append(json.getString("icon"));
        modifySql.append("', icon_type='");
        modifySql.append(json.getString("icon_type"));
        modifySql.append("', icon_url='");
        modifySql.append(json.getString("icon_url"));
        modifySql.append("', icon_color='");
        modifySql.append(json.getString("icon_color"));
        modifySql.append("', edition='");
        modifySql.append(json.getString("edition"));
        modifySql.append("', application_id='");
        modifySql.append(json.getString("applicationId"));
        modifySql.append("', personnel_modify_by=");
        modifySql.append(employeeId);
        modifySql.append(", datetime_modify_time=");
        modifySql.append(System.currentTimeMillis());
        modifySql.append(" where english_name = '");
        modifySql.append(bean);
        modifySql.append("'");
        
        int count = DAOUtil.executeUpdate(modifySql.toString());
        if (count < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        else
        {
            JSONObject moduleJson = findModuleByBean(token, bean);
            if (null != moduleJson)
            {
                // 修改文件夹名称
                Map<String, Object> m = new HashMap<>();
                m.put("token", token);
                m.put("name", json.getString("title"));
                m.put("id", moduleJson.getInteger("id"));
                m.put("type", "1");
                fileLibraryAppService.midfApplyName(m);
            }
        }
        return serviceResult;
    }
    
    @Override
    public JSONObject findModuleByBean(String token, String bean)
        throws Exception
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String moduleTable = DAOUtil.getTableName("application_module", companyId.toString());
        
        StringBuilder querySql = new StringBuilder();
        querySql.append("select * from ");
        querySql.append(moduleTable);
        querySql.append(" where english_name = '");
        querySql.append(bean);
        querySql.append("'");
        
        return DAOUtil.executeQuery4FirstJSON(querySql.toString());
    }
    
    /**
     * 获取所有模块列表
     * 
     * @param token
     * @return List
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public List<JSONObject> findAllModuleList(String token, String approvalFlag, String clientFlag)
        throws Exception
    {
        List<JSONObject> resultList = new ArrayList<JSONObject>();
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工角色
        JSONObject roleJson = null;
        String key = new StringBuilder("userInfo_").append(companyId).append("_").append(info.getEmployeeId()).toString();
        Object userInfoObj = JedisClusterHelper.get(key);
        if (null != userInfoObj)
        {
            Map<String, Object> userInfoMap = (Map)userInfoObj;
            roleJson = (JSONObject)userInfoMap.get("roleInfo");
        }
        else
        {
            roleJson = moduleDataAuthAppService.getRoleByUser(token);
        }
        if (roleJson == null || roleJson.get("id") == null)
        {
            return resultList;
        }
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        // 获取角色模块
        JSONArray result = moduleDataAuthAppService.getModuleAuthByRole(token, roleId, approvalFlag, clientFlag);
        if (result.size() > 0)
        {
            // 按照应用把模块分组
            Map<String, List<JSONObject>> moduleGroup = new HashMap<String, List<JSONObject>>();
            Iterator<Object> iterator = result.iterator();
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                String applicationId = obj.getString("application_id");
                if (moduleGroup.containsKey(applicationId))
                {
                    List<JSONObject> modules = moduleGroup.get(applicationId);
                    modules.add(obj);
                }
                else
                {
                    List<JSONObject> modules = new ArrayList<JSONObject>();
                    modules.add(obj);
                    moduleGroup.put(applicationId, modules);
                }
            }
            if (!"0".equals(clientFlag))
            {
                
                Map<String, String> reqmap = new HashMap<>();
                reqmap.put("token", token);
                JSONObject first = new JSONObject();
                first.put("name", "常用应用");
                first.put("modules", applicationModuleUsedAppService.getApplicationModuleUseds(reqmap));
                resultList.add(first);
            }
            // 匹配能看到的模块，按照应用模块组织结构返回
            String applicationTable = DAOUtil.getTableName("application", companyId.toString());
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ");
            querySql.append(applicationTable);
            querySql.append(" where ").append(Constant.FIELD_DEL_STATUS).append(" = 0");
            querySql.append(" order by topper;");
            List<JSONObject> list = DAOUtil.executeQuery4JSON(querySql.toString());
            for (Iterator ite = list.iterator(); ite.hasNext();)
            {
                JSONObject json = (JSONObject)ite.next();
                String id = json.getString("id");
                if (moduleGroup.containsKey(id))
                {
                    List<JSONObject> modules = moduleGroup.get(id);
                    Collections.sort(modules, new Comparator<Object>()
                    {
                        @Override
                        public int compare(Object o1, Object o2)
                        {
                            JSONObject stu1 = (JSONObject)o1;
                            JSONObject stu2 = (JSONObject)o2;
                            int topper1 = stu1.getIntValue("topper");
                            int topper2 = stu2.getIntValue("topper");
                            if (topper1 > topper2)
                            {
                                return 1;
                            }
                            else if (topper2 == topper1)
                            {
                                return 0;
                            }
                            else
                            {
                                return -1;
                            }
                        }
                    });
                    json.put("modules", modules);
                    resultList.add(json);
                }
            }
        }
        
        return resultList;
        
    }
    
    @Override
    public List<JSONObject> findAllModule(String token)
        throws Exception
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String appTable = DAOUtil.getTableName("application", companyId);
        String appModleTable = DAOUtil.getTableName("application_module", companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("select * from ").append(appModleTable);
        querySql.append(" where del_status = 0 and application_id in (select id from ").append(appTable);
        querySql.append(" where del_status= 0) and id in(select module_id from role_module_").append(companyId);
        querySql.append(" where role_id in (select role_id from employee_").append(companyId);
        querySql.append(" where id = ").append(info.getEmployeeId());
        querySql.append(") order by id) order by topper,datetime_create_time ");
        return DAOUtil.executeQuery4JSON(querySql.toString());
    }
    
    /**
     * 获取模块列表
     * 
     * @param token
     * @param applicationId
     * @return List
     */
    @Override
    public List<JSONObject> findModuleList(String token, Long applicationId)
        throws Exception
    {
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        String applicationTable = DAOUtil.getTableName("application", companyId);
        StringBuilder querySql = new StringBuilder();
        querySql.append("select m.*,a.name application_name from ");
        querySql.append(moduleTable);
        querySql.append(" m  left join ");
        querySql.append(applicationTable);
        querySql.append(" a  on m.application_id =a.id ");
        querySql.append(" where m.del_status = ").append(Constant.CURRENCY_ZERO);
        if (null != applicationId)
        {
            querySql.append(" and m.application_id = ");
            querySql.append(applicationId);
        }
        querySql.append(" order by m.topper;");
        return DAOUtil.executeQuery4JSON(querySql.toString());
        
    }
    
    @Override
    public List<JSONObject> findModuleListByAuth(String token, Long applicationId, String clientFlag)
        throws Exception
    {
        
        if (StringUtils.isEmpty(applicationId))
        {
            return null;
        }
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        String roleModuleTable = DAOUtil.getTableName("role_module", companyId);
        String moduleTable = DAOUtil.getTableName("application_module", companyId);
        String applicationTable = DAOUtil.getTableName("application", companyId);
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        StringBuilder querySql = new StringBuilder();
        querySql.append(" select t.*, (select  name from ").append(applicationTable).append(" where id = t.application_id ) as application_name from ").append(moduleTable).append(
            " t where id in (");
        querySql.append("select t2.id from ");
        querySql.append(roleModuleTable);
        querySql.append(" t1, ");
        querySql.append(moduleTable);
        querySql.append(" t2 where t1.module_id = t2.id and t1.role_id = ");
        querySql.append(roleId);
        querySql.append(" and t2.").append(Constant.FIELD_DEL_STATUS).append("=0 ");
        // 0：pc、手机都不显示 1:手机 2：pc 3：都显示
        if ("0".equals(clientFlag))
        {
            querySql.append(" and ( t2.terminal_app=2 or t2.terminal_app=3 ) ");
        }
        else
        {
            querySql.append(" and ( t2.terminal_app=1 or t2.terminal_app=3 ) ");
        }
        querySql.append(" and t2.application_id = ");
        querySql.append(applicationId);
        querySql.append(")  order by t.topper ");
        return DAOUtil.executeQuery4JSON(querySql.toString());
        
    }
    
    /**
     * 删除模块
     * 
     * @param token
     * @param moduleId
     * @return
     */
    @Override
    public ServiceResult<String> deleteModule(String token, Long moduleId)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        String applicationTable = DAOUtil.getTableName("application_module", companyId.toString());
        StringBuilder builder = new StringBuilder();
        builder.append(" update ").append(applicationTable).append(" set ").append(Constant.FIELD_DEL_STATUS).append("=1 where id=").append(moduleId);
        int delCount = DAOUtil.executeUpdate(builder.toString());
        if (delCount < 1)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
        }
        
        // 如果删除的模块在常用模块当中，也把该模块从常用模块中删除
        String tableName = DAOUtil.getTableName("application_module_used", companyId.toString());
        String moduleTableName = DAOUtil.getTableName("application_module", companyId.toString());
        builder.setLength(0);
        builder.append("select t1.module_id as id, t2.chinese_name ,t2.icon as icon , t2.english_name from ")
            .append(tableName)
            .append(" t1 left join ")
            .append(moduleTableName)
            .append(" t2 on t1.module_id=t2.id ")
            .append(" where t1.")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0 ")
            .append(" and t1.employee_id=")
            .append(employeeId)
            .append(" and t2.")
            .append(Constant.FIELD_DEL_STATUS)
            .append("=0")
            .append(" and module_id=")
            .append(moduleId);
        
        JSONObject obj = DAOUtil.executeQuery4FirstJSON(builder.toString());
        if (obj != null)
        {
            builder.setLength(0);
            builder.append(" delete from ").append(tableName).append(" where module_id=").append(obj.getString("id"));
            DAOUtil.executeUpdate(builder.toString());
        }
        // 删除布局
        builder.setLength(0);
        builder.append(" select english_name from ").append(applicationTable).append(" where id=").append(moduleId);
        JSONObject json = DAOUtil.executeQuery4FirstJSON(builder.toString());
        Document queryDoc = new Document();
        queryDoc.put("bean", json.getString("english_name"));
        queryDoc.put("companyId", companyId.toString());
        queryDoc.put("pageNum", "0");
        queryDoc.put("layoutState", Constant.LAYOUT_TYPE_ENABLE);
        Document result = LayoutUtil.findDocument(queryDoc, Constant.CUSTOMIZED_COLLECTION);
        if (result != null)
        {
            JSONObject resultJson = JSONObject.parseObject(result.toJson());
            String layoutId = resultJson.getJSONObject("_id").getString("$oid");
            LayoutUtil.removeModuleSetLayout(layoutId, Constant.CUSTOMIZED_COLLECTION);
        }
        
        // 删除关联模块
        LayoutUtil.removeRelationsByBean(companyId.toString(), json.getString("english_name"), "0");
        return serviceResult;
    }
    
    /**
     * 模块排序
     * 
     * @param requestBean
     * @return ServiceResult
     * @Description:
     */
    @Override
    public ServiceResult<String> sequencingModule(Map map)
        throws Exception
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        JSONObject json = JSONObject.parseObject((String)map.get("data"));
        if (StringUtils.isEmpty(json) || !json.containsKey("application_id") || !json.containsKey("ids"))
        {
            serviceResult.setCodeMsg(resultCode.get("common.req.param.error"), resultCode.getMsgZh("common.req.param.error"));
            return serviceResult;
        }
        String applicationId = json.getString("application_id");
        JSONArray array = json.getJSONArray("ids");
        Iterator<Object> iterator = array.iterator();
        String token = map.get("token").toString();
        
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String table = DAOUtil.getTableName("application_module", companyId);
        StringBuilder builder = new StringBuilder();
        int topper = 0;
        while (iterator.hasNext())
        {
            Object id = iterator.next();
            topper++;
            builder.append(" update ")
                .append(table)
                .append(" set topper=")
                .append("'")
                .append(topper)
                .append("'")
                .append(" where id=")
                .append(id)
                .append(" and application_id=")
                .append(applicationId)
                .append(";");
        }
        DAOUtil.executeUpdate(builder.toString());
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    /**
     * 获取规则筛选的初始化数据
     */
    @Override
    public JSONArray queryInitData(String token, String bean)
        throws Exception
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        JSONArray array = JSONParser4SQL.getFilterInitData(info.getCompanyId().toString(), bean, false);
        return array;
        
    }
    
    @Override
    public JSONArray getBeanInitData(String token, String bean)
        throws Exception
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        return JSONParser4SQL.getBeanInitData(info.getCompanyId().toString(), bean, false);
        
    }
    
    /**
     * 获取导航栏应用便签列表
     */
    @Override
    public JSONObject findPcAllModuleList(String token, int clientFlag)
        throws Exception
    {
        JSONObject jsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(token);
        String roleTable = DAOUtil.getTableName("role_module", info.getCompanyId());
        String moduleTable = DAOUtil.getTableName("application_module", info.getCompanyId());
        String usedTable = DAOUtil.getTableName(" application_module_used", info.getCompanyId());
        String applicationTable = DAOUtil.getTableName(" application", info.getCompanyId());
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        if (roleJson == null || roleJson.get("id") == null)
        {
            return jsonObject;
        }
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        // 获取我的应用版块
        StringBuilder builder = new StringBuilder();
        builder.append("SELECT id,name chinese_name,icon_type,icon_color,icon_url  from ")
            .append(applicationTable)
            .append("  where id in (")
            .append("SELECT")
            .append(" t2.application_id")
            .append(" FROM ")
            .append(roleTable)
            .append(" t1,")
            .append(moduleTable)
            .append(" t2  WHERE t1.module_id = t2. ID AND t1.role_id = ")
            .append(roleId)
            .append(" AND t2.del_status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" AND (");
        if (clientFlag == 1) // 是否是手机端
        {
            builder.append("t2.terminal_app = 1   OR t2.terminal_app = 3");
        }
        else
        {
            builder.append("t2.terminal_app = 2   OR t2.terminal_app = 3");
        }
        builder.append(") GROUP BY t2.application_id) ").append(" AND  del_status = ").append(Constant.CURRENCY_ZERO).append(" ORDER BY topper asc ");
        List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(builder.toString());
        jsonObject.put("myApplication", jsonList);
        // 获取常见应用
        StringBuilder querybuilder = new StringBuilder();
        querybuilder.append("select module_id id,chinese_name,english_name,icon_type,icon_color,icon_url  from ")
            .append(usedTable)
            .append(" where  del_status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and employee_id=")
            .append(info.getEmployeeId())
            .append(" order by id asc ");
        List<JSONObject> list = DAOUtil.executeQuery4JSON(querybuilder.toString());
        jsonObject.put("commonApplication", list);
        return jsonObject;
    }
    
    /**
     * 获取系统外观模版列表
     */
    @Override
    public List<JSONObject> querySystemFacadeList(String token)
        throws Exception
    {
        StringBuilder queryBuilder = new StringBuilder();
        List<JSONObject> list = new ArrayList<>();
        String systemTable = DAOUtil.getTableName("system_facade_template", "");
        queryBuilder.append(" select * from ").append(systemTable);
        list = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        return list;
    }
    
    /**
     * 获取审批模块
     */
    @Override
    public List<JSONObject> findApprovalModuleList(String token, String clientFlag)
    {
        List<JSONObject> resultList = new ArrayList<>();
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取员工角色
        JSONObject roleJson = null;
        Object userInfoObj = JedisClusterHelper.get(new StringBuilder("userInfo_").append(companyId).append("_").append(info.getEmployeeId()).toString());
        if (null != userInfoObj)
        {
            Map<String, Object> userInfoMap = (Map)userInfoObj;
            roleJson = (JSONObject)userInfoMap.get("roleInfo");
        }
        else
        {
            roleJson = moduleDataAuthAppService.getRoleByUser(token);
        }
        if (roleJson == null || roleJson.get("id") == null)
        {
            return resultList;
        }
        // 角色id.
        Integer roleId = roleJson.getInteger("id");
        // 获取角色模块
        JSONArray result = moduleDataAuthAppService.getModuleAuthByRole(token, roleId, "1", clientFlag);
        if (!result.isEmpty())
        {
            // 按照应用把模块分组
            Map<String, List<JSONObject>> moduleGroup = new HashMap<>();
            Iterator<Object> iterator = result.iterator();
            while (iterator.hasNext())
            {
                JSONObject obj = (JSONObject)iterator.next();
                String applicationId = obj.getString("application_id");
                if (moduleGroup.containsKey(applicationId))
                {
                    List<JSONObject> modules = moduleGroup.get(applicationId);
                    modules.add(obj);
                }
                else
                {
                    List<JSONObject> modules = new ArrayList<>();
                    modules.add(obj);
                    moduleGroup.put(applicationId, modules);
                }
            }
            // 匹配能看到的模块，按照应用模块组织结构返回
            String applicationTable = DAOUtil.getTableName("application", companyId.toString());
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from ");
            querySql.append(applicationTable);
            querySql.append(" where ").append(Constant.FIELD_DEL_STATUS).append(" = 0");
            querySql.append(" order by topper;");
            List<JSONObject> list = DAOUtil.executeQuery4JSON(querySql.toString());
            for (Iterator ite = list.iterator(); ite.hasNext();)
            {
                JSONObject json = (JSONObject)ite.next();
                String id = json.getString("id");
                if (moduleGroup.containsKey(id))
                {
                    List<JSONObject> modules = moduleGroup.get(id);
                    Collections.sort(modules, new Comparator<Object>()
                    {
                        @Override
                        public int compare(Object o1, Object o2)
                        {
                            JSONObject stu1 = (JSONObject)o1;
                            JSONObject stu2 = (JSONObject)o2;
                            int topper1 = stu1.getIntValue("topper");
                            int topper2 = stu2.getIntValue("topper");
                            if (topper1 > topper2)
                            {
                                return 1;
                            }
                            else if (topper2 == topper1)
                            {
                                return 0;
                            }
                            else
                            {
                                return -1;
                            }
                        }
                    });
                    resultList.addAll(modules);
                }
            }
        }
        
        return resultList;
    }
    
}
