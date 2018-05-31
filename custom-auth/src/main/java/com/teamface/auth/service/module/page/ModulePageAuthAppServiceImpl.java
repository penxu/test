package com.teamface.auth.service.module.page;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.auth.service.module.ModuleDataAuthAppServiceImpl;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.auth.ModulePageAuthAppService;

/**
 * @Title:模块页面权限接口实现
 * @Description:
 * @Author:Administrator
 * @Since:2017年11月27日
 * @Version:1.1.0
 */
@Service("modulePageAuthAppService")
public class ModulePageAuthAppServiceImpl implements ModulePageAuthAppService
{
    private static Logger log = Logger.getLogger(ModuleDataAuthAppServiceImpl.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    ModuleDataAuthAppService moduleDataAuthAppService;
    
    /**
     * @param token
     * @param moduleId 模块id
     * @param pageNum 页码
     * @param beanName 模块名称
     * @return ServiceResult
     * @Description:保存页面信息
     */
    @Override
    public ServiceResult<String> savePageInfo(String token, Long moduleId, Integer pageNum, String beanName)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            JSONObject modulePageObj = new JSONObject();
            modulePageObj.put("module_id", moduleId);
            modulePageObj.put("page_num", pageNum);
            modulePageObj.put("name", beanName);
            modulePageObj.put(Constant.FIELD_DEL_STATUS, 0);
            modulePageObj.put(Constant.FIELD_CREATE_BY, employeeId);
            modulePageObj.put(Constant.FIELD_CREATE_TIME, System.currentTimeMillis());
            JSONObject page = new JSONObject();
            page.put("data", modulePageObj.toString());
            page.put("bean", "application_module_page");
            String insertSql = JSONParser4SQL.getInsertSql(page, companyId.toString());
            int count = DAOUtil.executeUpdate(insertSql);
            if (count < 1)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            /*JSONObject addJson = new JSONObject();
            addJson.put("moduleId", moduleId);
            ServiceResult<String> addResult = moduleDataAuthAppService.addAuthAndBut(token, addJson.toString(), moduleId, true);
            if (!addResult.isSucces())
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }*/
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return serviceResult;
    }

    /**
     * 获取指定模块的页面列表
     * 
     * @param token
     * @param module 模块名
     * @return JSONArray
     */
    @Override
    public JSONArray getModulePageList(String token, String module)
    {
        JSONArray result = new JSONArray();
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token); 
        Long companyId = info.getCompanyId();
        
        String pageTable = DAOUtil.getTableName("application_module_page", companyId);
        
        // 1. 获取module下所有的页面集合
        StringBuilder queryModulePage = new StringBuilder();
        queryModulePage.append("select * from ");
        queryModulePage.append(pageTable);
        queryModulePage.append(" where module_id = ");
        queryModulePage.append(module);
        queryModulePage.append(" and company_id=");
        queryModulePage.append(companyId);
        List<JSONObject> modulePageList = DAOUtil.executeQuery4JSON(queryModulePage.toString());
        
        if (!modulePageList.isEmpty())
        {
            String pageAuthTable = DAOUtil.getTableName("module_page_auth", companyId);
            // 2. 获取每一个页面下的权限
            for (JSONObject pageJson : modulePageList)
            {
                Object pageId = pageJson.get("id");
                StringBuilder queryPageAuthList = new StringBuilder();
                queryPageAuthList.append("select * from ");
                queryPageAuthList.append(pageAuthTable);
                queryPageAuthList.append(" where page_id=");
                queryPageAuthList.append(pageId);
                List<JSONObject> pageAuthList = DAOUtil.executeQuery4JSON(queryPageAuthList.toString());
                String roleIds = null;
                if (!pageAuthList.isEmpty())
                {
                    // 组装人员
                    JSONObject pageAuthJson = pageAuthList.get(0);
                    roleIds = pageAuthJson.getString("role_id");
                }
                pageJson.put("roleIds", roleIds);
                result.add(pageJson);
            }
        }
        return result;
    }
    
    /**
     * @param companyId 公司id
     * @param module 模块名称
     * @param pageNum 页码
     * @return JSONObject
     * @Description:根据页码获取模块页面
     */
    @Override
    public JSONObject findPageByPageNum(Long companyId, String module, Integer pageNum)
    {
        StringBuilder pageSql = new StringBuilder();
        String pageTable = DAOUtil.getTableName("application_module_page", companyId);
        pageSql.append("select * from ");
        pageSql.append(pageTable);
        pageSql.append(" where page_num=");
        pageSql.append(pageNum);
        pageSql.append(" and name='");
        pageSql.append(module);
        pageSql.append("'");
        return DAOUtil.executeQuery4FirstJSON(pageSql.toString());
    }

    /**
     * @param companyId
     * @param beanName
     * @param moduleId
     * @return List
     * @Description:获取多页面列表
     */
    @Override
    public List<JSONObject> findMorePageLayout(Long companyId, String beanName)
    {
        List<JSONObject> result = new ArrayList<JSONObject>();
        try
        {
            Document queryDoc = new Document();
            queryDoc.put("bean", beanName);
            queryDoc.put("companyId", companyId);
            
            List<JSONObject> pageLayoutList = LayoutUtil.findModuleSetLayout(queryDoc, Constant.CUSTOMIZED_COLLECTION);
            if(!pageLayoutList.isEmpty()){
                for (JSONObject jsonObject : pageLayoutList)
                {
                    if(!jsonObject.getString("pageNum").equals("0")){
                        result.add(jsonObject);
                    }
                }
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 对模块页面进行权限设置
     * 
     * @param token
     * @param reqJsonStr
     * @return ServiceResult
     */
    @Override
    public ServiceResult<String> modifyModulePageAuth(String token, String reqJsonStr)
    {
        ServiceResult<String> serviceResult = new ServiceResult<String>();
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        try
        {
            // 请求参数
            JSONObject reqJson = JSONObject.parseObject(reqJsonStr);
            Integer moduleId = reqJson.getInteger("moduleId");
            Integer pageNum = reqJson.getInteger("pageNum");
            String roleIds = reqJson.getString("roleIds");
            
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token); 
            Long companyId = info.getCompanyId();
            
            // 获取页面
            String pageTable = DAOUtil.getTableName("application_module_page", companyId);
            StringBuilder queryPage = new StringBuilder();
            queryPage.append("select * from ");
            queryPage.append(pageTable);
            queryPage.append(" where module_id = ");
            queryPage.append(moduleId);
            queryPage.append(" and page_num = ");
            queryPage.append(pageNum);
            queryPage.append(" and company_id=");
            queryPage.append(companyId);
            List<JSONObject> resultPage = DAOUtil.executeQuery4JSON(queryPage.toString());
            if (resultPage.isEmpty())
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            JSONObject pageInfo = resultPage.get(0);
            
            // 删除页面权限
            String pageAuthTable = DAOUtil.getTableName("module_page_auth", companyId);
            StringBuilder deletePageAuth = new StringBuilder();
            deletePageAuth.append("delete from ");
            deletePageAuth.append(pageAuthTable);
            deletePageAuth.append(" where page_id = ");
            deletePageAuth.append(pageInfo.getInteger("id"));
            DAOUtil.executeUpdate(deletePageAuth.toString());
            
            // 设置页面权限
            StringBuilder insertPageAuth = new StringBuilder();
            insertPageAuth.append("insert into ");
            insertPageAuth.append(insertPageAuth);
            insertPageAuth.append("(company_id, page_id, role_id) values(");
            insertPageAuth.append(companyId);
            insertPageAuth.append(", ");
            insertPageAuth.append(pageInfo.getInteger("id"));
            insertPageAuth.append(", ");
            insertPageAuth.append(roleIds);
            insertPageAuth.append(")");
            DAOUtil.executeUpdate(insertPageAuth.toString());
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return serviceResult;
    }
    
    /**
     * @param token
     * @return JSONObject(page_num模块页码, name模块英文名称)
     * @Description:获取用户页面权限
     */
    @Override
    public JSONObject getPageAuthByEmployee(String token)
    {
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取用户页面权限
        JSONObject pageAuthJson = this.getPageAuthByEmployee(token);
        if(null == pageAuthJson){
            return null;
        }
        // 页面id
        Integer pageId = pageAuthJson.getInteger("id");
        
        String modulePageTable = DAOUtil.getTableName("application_module_page", companyId);
        StringBuilder queryModulePage = new StringBuilder();
        queryModulePage.append("select * from ");
        queryModulePage.append(modulePageTable);
        queryModulePage.append(" where id = ");
        queryModulePage.append(pageId);
        
        return DAOUtil.executeQuery4FirstJSON(queryModulePage.toString());
    }

    /**
     * @param token
     * @return JSONObject
     * @Description:获取用户页面信息
     */
    @Override
    public JSONObject getPageByEmployee(String token)
    {
        //return null;//页面权限暂时做不了，全部取标准页面权限
        JSONObject result = null;
        
        // 解析token
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        
        // 获取用户角色
        JSONObject roleJson = moduleDataAuthAppService.getRoleByUser(token);
        if(roleJson == null){
            return result;
        }
        
        // 员工所属角色
        Integer roleId = roleJson.getInteger("id");
        String pageAuthTable = DAOUtil.getTableName("module_page_auth", companyId);
        StringBuilder queryPageAuth = new StringBuilder();
        queryPageAuth.append("select * from ");
        queryPageAuth.append(pageAuthTable);
        queryPageAuth.append(" where string_to_array(role_id,',') @> array['");
        queryPageAuth.append(roleId);
        queryPageAuth.append("']");
        List<JSONObject> resultList = DAOUtil.executeQuery4JSON(queryPageAuth.toString());
        if(!resultList.isEmpty()){
            result = resultList.get(0);
        }
        return result;
    }
    
    
}
