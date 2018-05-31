package com.teamface.filelibrary.service;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.support.SQLStateSQLExceptionTranslator;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.OSSUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.project.ProjectLibraryService;
import com.teamface.custom.service.project.ProjectService;

@Service("projectLibraryService")
public class ProjectLibraryServiceImpl implements ProjectLibraryService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(ProjectLibraryServiceImpl.class);
    
    @Autowired
    private ProjectService projectService;
    
    /**
     * 创建文件夹
     */
    @Transactional
    @Override
    public boolean savaLibrary(String token, JSONObject jsonObject)
    {
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            StringBuilder buff = new StringBuilder();
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            buff.append(Constant.PROJECT_NAME).append(info.getCompanyId()).append("/");
            int type = jsonObject.getInteger("type");
            // 分层 0 项目 1 分组 2 列表 路径拼接
            if (type == 0)
            {
                buff.append(jsonObject.getLong("id")).append("/");
                if (jsonObject.get("tempId") == null || jsonObject.getLong("tempId") == 0) // 如果模板ID为空，则需要生产默认节点信息
                {
                    // 获取子节点信息
                    JSONObject jsonList = projectService.querySubNode(token, jsonObject.getLong("id"));
                    if (null != jsonList)
                    {
                        JSONArray array = jsonList.getJSONArray("dataList");
                        List<List<Object>> batchValue = new ArrayList<>();
                        for (int i = 0; i < array.size(); i++)
                        {
                            StringBuilder pathBuff = new StringBuilder();
                            List<Object> model = new ArrayList<>();
                            JSONObject json = (JSONObject)array.get(i);
                            pathBuff.append(jsonObject.getLong("id")).append("/").append(json.getLong("id")).append("/");
                            model.add(Constant.CURRENCY_ONE);
                            model.add(json.getLong("id"));
                            model.add(json.getString("name"));
                            model.add(pathBuff);
                            model.add(jsonObject.getLong("id"));
                            model.add("#FABC01");
                            model.add(info.getEmployeeId());
                            model.add(System.currentTimeMillis());
                            batchValue.add(model);
                        }
                        if (!batchValue.isEmpty())
                        {
                            StringBuilder insertSql = new StringBuilder();
                            insertSql.append("insert into ").append(libraryTable).append(
                                "(project_type,data_id,file_name,url,parent_id,color,create_by,create_time) values(?,?,?,?,?,?,?,?)");
                            int count = DAOUtil.executeUpdate(insertSql.toString(), batchValue, 100);
                            if (count <= 0)
                            {
                                return false;
                            }
                        }
                    }
                }
            }
            else if (type == 1)
            {
                buff.append(jsonObject.getString("parent_id")).append("/").append(jsonObject.getLong("id")).append("/");
            }
            else if (type == 2)
            {
                JSONObject object = projectService.queryMainNode(token, jsonObject.getLong("parent_id"));
                buff.append(object.getString("id")).append("/").append(jsonObject.getString("parent_id")).append("/").append(jsonObject.getLong("id") + "/");
            }
            StringBuilder insertBuilder = new StringBuilder();
            // 保存创建数据
            insertBuilder.append("insert into ");
            insertBuilder.append(libraryTable);
            insertBuilder.append("(project_type,data_id,file_name,url,parent_id,color,sort,create_by,create_time) values('");
            insertBuilder.append(type);
            insertBuilder.append("',");
            insertBuilder.append(jsonObject.getLong("id"));
            insertBuilder.append(",'");
            insertBuilder.append(jsonObject.getString("name"));
            insertBuilder.append("','");
            insertBuilder.append(buff);
            insertBuilder.append("',");
            insertBuilder.append(jsonObject.getLong("parent_id"));
            insertBuilder.append(",");
            insertBuilder.append("'#FABC01',");
            insertBuilder.append(jsonObject.getInteger("sort"));
            insertBuilder.append(",").append(info.getEmployeeId()).append(",").append(System.currentTimeMillis()).append(")");
            int sum = DAOUtil.executeUpdate(insertBuilder.toString());
            if (sum <= 0)
            {
                return false;
            }
            // oss操作
            boolean flag = OSSUtil.getInstance().createProjectLibrary(buff.toString());
            if (!flag)
            {
                new SQLStateSQLExceptionTranslator().translate("PreparedStatementCallback", "jdbc insert", new SQLException());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return false;
        }
        
        return true;
    }
    
    /**
     * 获取分组任务列表
     */
    @Override
    public List<JSONObject> queryLibraryList(Map<String, String> map)
    {
        StringBuilder queryBuilder = new StringBuilder();
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        queryBuilder.append(" select   id,file_name,data_id from  ").append(libraryTable).append(" where parent_id = ").append(map.get("id")).append(" and del_status=").append(
            Constant.CURRENCY_ZERO);
        return DAOUtil.executeQuery4JSON(queryBuilder.toString());
    }
    
    /**
     * 添加文件夹 缺少权限判断
     */
    @Transactional
    @Override
    public ServiceResult<String> savaFileLibrary(String token, JSONObject jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            StringBuilder queryBilder = new StringBuilder();
            // 获取上级目录
            queryBilder.append("select * from  ").append(libraryTable).append(" where data_id =").append(jsonObject.getLong("parent_id")).append(" and library_type = ").append(
                jsonObject.getString("type"));
            JSONObject json = DAOUtil.executeQuery4FirstJSON(queryBilder.toString());
            if (null == json)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            // 拼接创建路径
            StringBuilder pathBuf = new StringBuilder();
            Long id = BusinessDAOUtil.geCurrval4Table("project_library", info.getCompanyId().toString());
            pathBuf.append(json.getString("url")).append(id).append("/");
            
            // 同步文件库
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into ")
                .append(libraryTable)
                .append("(file_name,url,parent_id,color,create_by,create_time,library_type) values('")
                .append(jsonObject.getString("name"))
                .append("','")
                .append(pathBuf)
                .append("',")
                .append(json.getLong("id"))
                .append(",")
                .append("'#FABC01',")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(",")
                .append(jsonObject.getString("type"))
                .append(")");
            
            int count = DAOUtil.executeUpdate(insertBuilder.toString());
            if (count <= 0)
            {
                new SQLStateSQLExceptionTranslator().translate("PreparedStatementCallback", "jdbc insert", new SQLException());
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            boolean flag = OSSUtil.getInstance().createProjectLibrary(pathBuf.toString());
            if (!flag)
            {
                new SQLStateSQLExceptionTranslator().translate("PreparedStatementCallback", "jdbc insert", new SQLException());
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 修改文件夹 缺少权限判断
     */
    @Override
    public ServiceResult<String> editLibrary(String token, JSONObject jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            StringBuilder editBilder = new StringBuilder();
            editBilder.append(" update ").append(libraryTable).append(" set file_name = '").append(jsonObject.get("name")).append("' where id = ").append(jsonObject.getLong("id"));
            int sum = DAOUtil.executeUpdate(editBilder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除文件夹 缺少权限判断
     */
    @Override
    public ServiceResult<String> delLibrary(String token, JSONObject jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            StringBuilder editBilder = new StringBuilder();
            editBilder.append(" update ").append(libraryTable).append(" set del_status = ").append(Constant.CURRENCY_ONE).append(" where id = ").append(jsonObject.getLong("id"));
            int sum = DAOUtil.executeUpdate(editBilder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 任务列表
     */
    @Override
    public List<JSONObject> queryTaskLibraryList(Map<String, String> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        StringBuilder queryBuilder = new StringBuilder();
        List<JSONObject> list = new ArrayList<>();
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        queryBuilder.append("select  id,file_name,data_id  from  ").append(libraryTable).append("   where parent_id = ").append(map.get("id"));
        list = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        return list;
    }
    
    /**
     * 移动文件
     */
    @Override
    public boolean shiftProjectLibrary(InfoVo info, JSONObject jsonObject)
    {
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" select * from ").append(libraryTable).append(" where data_id = ").append(jsonObject.get("current_id")).append(" and project_type=").append(
            Constant.CURRENCY_TWO);
        JSONObject json = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString()); // 当前信息
        if (null == json)
        {
            return false;
        }
        StringBuilder editBuilder = new StringBuilder();
        editBuilder.append("update  ")
            .append(libraryTable)
            .append(" set parent_id = ")
            .append(json.getLong("parent_id"))
            .append(" where data_id = ")
            .append(jsonObject.get("worn_id"))
            .append(" and project_type =")
            .append(Constant.CURRENCY_TWO);
        int count = DAOUtil.executeUpdate(editBuilder.toString());
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 复制文件夹
     */
    @Override
    public boolean copyProjectLibrary(InfoVo info, JSONObject jsonObject)
    {
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" select * from ").append(libraryTable).append(" where data_id = ").append(jsonObject.get("current_id")).append(" and project_type=").append(
            Constant.CURRENCY_TWO);
        JSONObject json = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString()); // 当前信息
        StringBuilder insertBuilder = new StringBuilder();
        insertBuilder.append("insert into ");
        insertBuilder.append(libraryTable);
        insertBuilder.append("(project_type,data_id,file_name,size,url,parent_id,suffix,type,create_by,create_time) values(");
        insertBuilder.append(json.getString("project_type"));
        insertBuilder.append(",");
        insertBuilder.append(json.getLong("data_id"));
        insertBuilder.append(",'");
        insertBuilder.append(json.getString("file_name"));
        insertBuilder.append("',");
        insertBuilder.append(json.getString("size"));
        insertBuilder.append(",'");
        insertBuilder.append(json.getString("url"));
        insertBuilder.append("',");
        insertBuilder.append(jsonObject.get("worn_id"));
        insertBuilder.append(",'");
        insertBuilder.append(json.getString("suffix"));
        insertBuilder.append("','");
        insertBuilder.append(json.getString("type"));
        insertBuilder.append("',");
        insertBuilder.append(info.getEmployeeId());
        insertBuilder.append(",");
        insertBuilder.append(System.currentTimeMillis());
        insertBuilder.append(")");
        int count = DAOUtil.executeUpdate(insertBuilder.toString());
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 删除任务
     */
    @Override
    public boolean delProjectLibrary(String token, JSONObject jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        StringBuilder editBuilder = new StringBuilder();
        editBuilder.append("update ")
            .append(libraryTable)
            .append(" set del_status=")
            .append(Constant.CURRENCY_ONE)
            .append(" where data_id = ")
            .append(jsonObject.getLong("id"))
            .append(" and project_type =")
            .append(jsonObject.getString("type"));
        int count = DAOUtil.executeUpdate(editBuilder.toString());
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 文库共享
     */
    @Override
    public ServiceResult<String> sharLibrary(String token, JSONObject jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String shareTable = DAOUtil.getTableName("project_library_share", info.getCompanyId());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        String[] employeeId = jsonObject.getString("employee_id").split(",");
        List<List<Object>> batchValues = new ArrayList<>();
        for (String employee : employeeId)
        {
            List<Object> model = new ArrayList<>();
            model.add(jsonObject.getLong("id")); // 数据id
            model.add(Long.valueOf(employee)); // 员工id
            model.add(System.currentTimeMillis()); // 时间
            batchValues.add(model);
        }
        StringBuilder insertBuilder = new StringBuilder();
        insertBuilder.append("insert into  ").append(shareTable).append("(library_id,share_by,create_time) values(?,?,?)");
        int count = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 1000);
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 文件查看列表
     */
    @Override
    public List<JSONObject> queryFileLibraryList(Map<String, String> map)
    {
        StringBuilder queryBuilder = new StringBuilder();
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        queryBuilder.append("select  id,file_name,data_id from ").append(libraryTable).append(" where data_id  in  (").append("").append(")");
        return DAOUtil.executeQuery4JSON(queryBuilder.toString());
    }
    
    /**
     * 模块数据ID
     * 
     */
    @Override
    public boolean editModuleDataId(String token, List<JSONObject> jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        List<List<Object>> batchValues = new ArrayList<>();
        List<List<Object>> batchValue = new ArrayList<>();
        for (int i = 0; i < jsonObject.size(); i++)
        {
            List<Object> model = new ArrayList<>();
            List<Object> newModel = new ArrayList<>();
            JSONObject object = jsonObject.get(i);
            String url = object.getString("url").substring(object.getString("url").lastIndexOf("fileName=") + 9, object.getString("url").length());
            model.add(object.getLong("id"));
            model.add(url);
            batchValues.add(model);
            newModel.add(object.getLong("id"));
            newModel.add(Constant.CURRENCY_THREE);
            batchValue.add(newModel);
        }
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        StringBuilder updateBuilder =
            new StringBuilder("update ").append(libraryTable).append(" set  del_status = ").append(Constant.CURRENCY_ONE).append("  where data_id = ? and project_type = ? ");
        int sum = DAOUtil.executeUpdate(updateBuilder.toString(), batchValue, 1000); // 批量修改所属那条数据的ID状态
        LOG.info("批量修改所属那条数据的ID状态" + sum);
        StringBuilder editBuilder = new StringBuilder();
        editBuilder.append("update ").append(libraryTable).append(" set  del_status = ").append(Constant.CURRENCY_ZERO).append(" , data_id = ?  where url = ?");
        int count = DAOUtil.executeUpdate(editBuilder.toString(), batchValues, 1000); // 批量修改所属那条数据的ID
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
    /**
     * 获取项目列表
     */
    @Override
    public List<JSONObject> queryProjectLibraryList(Map<String, String> map)
    {
        StringBuilder queryBuilder = new StringBuilder();
        InfoVo info = TokenMgr.obtainInfo(map.get("token"));
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        queryBuilder.append("select  id,file_name,data_id from ").append(libraryTable).append(" where data_id  in  (").append(map.get("id")).append(") and parent_id  is null");
        return DAOUtil.executeQuery4JSON(queryBuilder.toString());
    }
    
    /**
     * 修改名称排序信息
     */
    @Override
    public boolean editLibraryInfo(String token, JSONObject jsonObject)
    {
        StringBuilder editBuilder = new StringBuilder();
        InfoVo info = TokenMgr.obtainInfo(jsonObject.getString("token"));
        String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
        editBuilder.append("update ").append(libraryTable);
        editBuilder.append(" set del_status = '").append(Constant.CURRENCY_ZERO);
        if (null != jsonObject.getString("name") && !"".equals(jsonObject.getString("name")))
        {
            editBuilder.append(", name = '").append(jsonObject.getString("name")).append("'");
        }
        if (null != jsonObject.getInteger("sort"))
        {
            editBuilder.append(", sort = ").append(jsonObject.getInteger("sort"));
        }
        editBuilder.append(" where data_id = ");
        editBuilder.append(jsonObject.getLong("id"));
        editBuilder.append(" and project_type = ");
        editBuilder.append(jsonObject.getString("type"));
        editBuilder.append(" and del_status =");
        editBuilder.append(Constant.CURRENCY_ZERO);
        int count = DAOUtil.executeUpdate(editBuilder.toString());
        if (count <= 0)
        {
            return false;
        }
        return true;
    }
    
}
