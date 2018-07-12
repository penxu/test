package com.teamface.filelibrary.service;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.model.ServiceResult;
import com.teamface.common.msg.util.ParseTxtFromFile;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.OSSUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.application.ApplicationAppService;
import com.teamface.custom.service.auth.ModuleDataAuthAppService;
import com.teamface.custom.service.email.MailOperationService;
import com.teamface.custom.service.library.FileLibraryAppService;
import com.teamface.custom.service.module.ModuleAppService;

@Service("fileLibraryAppService")
public class FileLibraryAppServiceImpl implements FileLibraryAppService
{
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    private static final Logger LOG = LogManager.getLogger(FileLibraryAppServiceImpl.class);
    
    @Autowired
    private ApplicationAppService applicationAppService;
    
    @Autowired
    private ModuleAppService moduleAppService;
    
    @Autowired
    private ModuleDataAuthAppService moduleDataAuthAppService;
    
    @Autowired
    private MailOperationService mailOperationService;
    
    /**
     * 创建应用及模块时调用OSS创建相应文件夹
     * 
     */
    @Override
    public boolean savaLibrary(String token, JSONObject jsonObject)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        try
        {
            LOG.error("时间" + System.currentTimeMillis() + " savaLibrary 名称  + " + jsonObject.get("chinese_name"));
            StringBuilder buff = new StringBuilder();
            buff.append(Constant.APPLY_NAME).append(info.getCompanyId()).append("/");
            int type = jsonObject.getInteger("type");
            if (type == 0)
            {
                buff.append(jsonObject.getLong("id")).append(Constant.APPLY_NAME).append("/");
            }
            else if (type == 1)
            {
                String bean = jsonObject.getString("english_name");
                buff.append(jsonObject.getString("parent_id")).append(Constant.APPLY_NAME).append("/").append(bean).append("/");
            }
            boolean flag = OSSUtil.getInstance().addFolder(Constant.FLIE_LIBRARY_NAME, buff.toString());
            if (flag)
            {
                String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
                StringBuilder insertBuilder = new StringBuilder("insert into ").append(catalogTable)
                    .append("(model_id,name,url,parent_id,table_id,color,type,create_by,create_time) values(")
                    .append(jsonObject.getLong("id"))
                    .append(",'")
                    .append(jsonObject.getString("chinese_name"))
                    .append("','")
                    .append(buff)
                    .append("',")
                    .append(jsonObject.getLong("parent_id"))
                    .append(",2,'#FABC01',")
                    .append(type)
                    .append(",")
                    .append(info.getEmployeeId())
                    .append(",")
                    .append(System.currentTimeMillis())
                    .append(")");
                
                int sum = DAOUtil.executeUpdate(insertBuilder.toString());
                if (sum <= 0)
                {
                    return false;
                }
            }
            else
            {
                return false;
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
     * 查看应用根目录列表
     * 
     */
    @Override
    public JSONObject queryApplyList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        String id = applicationAppService.getApplicationsByUser(map);
        if (StringUtils.isBlank(id))
        {
            return result;
        }
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder buff = new StringBuilder();
        buff.append("select c.*,e.employee_name from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id   where c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id is null  and c.model_id in (")
            .append(id)
            .append(") ")
            .append(" order by c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(buff.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 查看应用子级目录
     */
    @Override
    public JSONObject queryApplyPartList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String applicationId = moduleAppService.getModulesByApplication(map);
        if (StringUtils.isBlank(applicationId))
        {
            return result;
        }
        StringBuilder buff = new StringBuilder();
        buff.append("select c.*,e.employee_name from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where  t.table_id =")
            .append(map.get("type"))
            .append(" and parent_id = ")
            .append(map.get("id"))
            .append("  and c.model_id in (")
            .append(applicationId)
            .append(")");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(buff.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 修改应用名称
     * 
     */
    @Override
    public boolean midfApplyName(Map<String, Object> map)
    {
        LOG.error("时间" + System.currentTimeMillis() + " midfApplyName 名称 " + map.get("name"));
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            StringBuilder buff = new StringBuilder("update ").append(catalogTable)
                .append(" set name='")
                .append(map.get("name"))
                .append("' where model_id = ")
                .append(map.get("id"))
                .append(" and type  = ")
                .append(map.get("type"));
            int count = DAOUtil.executeUpdate(buff.toString());
            if (count <= 0)
            {
                return false;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return true;
    }
    
    /**
     * 公司文件添加文件夹
     * 
     */
    @Override
    public ServiceResult<String> savaFileLibrary(Map<String, Object> map)
    {
        LOG.debug("进入创建文件夹==============================");
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            Long id = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            if (id > 1)
            {
                id = id + 1L;
            }
            LOG.debug("获取目录==============================");
            StringBuffer buff = new StringBuffer();
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            if (StringUtils.isBlank(map.get("parent_id").toString()))
            {
                buff.append(Constant.COMPNY_NAME).append(info.getCompanyId()).append("/").append(id).append(Constant.COMPNY_NAME).append("/");
            }
            else
            {
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("select count(1) from ");
                queryBuilder.append(manageTable);
                queryBuilder.append(" where file_id = ");
                queryBuilder.append(map.get("parent_id"));
                queryBuilder.append(" and employee_id = ");
                queryBuilder.append(info.getEmployeeId());
                
                int count = DAOUtil.executeCount(queryBuilder.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("postprocess.file.handle.library.error"), resultCode.getMsgZh("postprocess.file.handle.library.error"));
                    return serviceResult;
                }
                
                StringBuilder sql = new StringBuilder();
                sql.append("select * from  ");
                sql.append(catalogTable);
                sql.append(" where id = ");
                sql.append(map.get("parent_id"));
                JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(sql.toString());
                buff.append(jsonObject.getString("url")).append(id).append(Constant.COMPNY_NAME).append("/");
            }
            LOG.debug("获取链接==============================");
            String parentId = map.get("parent_id").equals("") ? null : map.get("parent_id").toString();
            StringBuilder buf = new StringBuilder("insert into ").append(catalogTable)
                .append("(name,url,parent_id,color,table_id,create_by,create_time) values('")
                .append(map.get("name"))
                .append("','")
                .append(buff)
                .append("',")
                .append(parentId)
                .append(",'")
                .append(map.get("color"))
                .append("',")
                .append(map.get("style"))
                .append(",")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int count = DAOUtil.executeUpdate(buf.toString()); // 应用目录表
            LOG.debug("插入数据==============================");
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            LOG.debug("type==============================");
            Long belogid = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
            if (StringUtils.isNotBlank(map.get("type").toString()))
            {
                String belongTable = DAOUtil.getTableName("catalog_belong", info.getCompanyId());
                StringBuilder builder =
                    new StringBuilder("insert into ").append(belongTable).append("(file_id,type) values(").append(belogid).append(",'").append(map.get("type")).append("')");
                int num = DAOUtil.executeUpdate(builder.toString()); // 目录设置表
                if (num <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
                String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
                if (Integer.parseInt(map.get("type").toString()) == 0) // 如果公开则默认添加全公司人员
                {
                    StringBuilder buil = new StringBuilder("select * from  ").append(employeeTable).append(" where del_status=").append(Constant.CURRENCY_ZERO);
                    List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(buil.toString());
                    List<List<Object>> batchValues = new ArrayList<>();
                    String manageBy = map.get("manage_by").toString();
                    String[] idArray = manageBy.split(",");
                    for (int i = 0; i < jsonList.size(); i++)
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(belogid); // 数据ID
                        model.add(jsonList.get(i).getLong("id")); // 员工id
                        model.add(jsonList.get(i).getLong("status")); // 是否显示
                        model.add(Constant.CURRENCY_ONE);
                        boolean fals = true;
                        for (String menBy : idArray)
                        {
                            if (jsonList.get(i).getLong("id").equals(Long.parseLong(menBy)))
                            {
                                fals = false;
                            }
                        }
                        if (fals == true)
                        {
                            batchValues.add(model);
                        }
                    }
                    if (batchValues.size() > 0)
                    {
                        StringBuilder queryBuilder = new StringBuilder("insert into ").append(settingTable).append(" (file_id,employee_id,sign,preview) values(?,?,?,?)");
                        int countSum = DAOUtil.executeUpdate(queryBuilder.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
            }
            LOG.debug("parent_id==============================");
            if (StringUtils.isNotBlank(map.get("parent_id").toString()))
            {
                if (Integer.parseInt(map.get("style").toString()) != Constant.CURRENCY_THREE)
                {
                    String sb = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(parentId), Constant.CURRENCY_ZERO, "catalog");
                    if (StringUtils.isNotBlank(sb))
                    { // 获取父级所有管理员
                        StringBuilder queryBuf = new StringBuilder("select * from ").append(manageTable).append(" where file_id in (").append(sb).append(") and sign_type=").append(
                            Constant.CURRENCY_ZERO);
                        List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(queryBuf.toString());
                        List<List<Object>> batchValues = new ArrayList<>();
                        for (int i = 0; i < jsonObject.size(); i++)
                        {
                            JSONObject json = jsonObject.get(i);
                            List<Object> model = new ArrayList<>();
                            model.add(belogid); // 数据ID
                            model.add(json.getLong("employee_id")); // 员工id
                            model.add(Constant.CURRENCY_ONE);
                            batchValues.add(model);
                        }
                        StringBuilder queryBuilder = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id,sign_type) values(?,?,?)");
                        int countSum = DAOUtil.executeUpdate(queryBuilder.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
            }
            LOG.debug("管理员==============================" + map.get("manage_by").toString());
            String manageBy = map.get("manage_by").toString();
            if (StringUtils.isNoneBlank(manageBy))
            { // 管理员
                String[] idArray = manageBy.split(",");
                if (idArray.length > 0)
                {
                    List<List<Object>> batchValues = new ArrayList<>();
                    for (String menBy : idArray)
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(belogid); // 数据ID
                        model.add(Long.parseLong(menBy)); // 员工id
                        StringBuilder countBuf =
                            new StringBuilder("select  count(*) from ").append(manageTable).append(" where file_id = ").append(id).append(" and employee_id=").append(
                                Long.parseLong(menBy));
                        int sum = DAOUtil.executeCount(countBuf.toString());
                        if (sum <= 0) // 是否存在
                        {
                            batchValues.add(model);
                        }
                    }
                    if (batchValues.size() > 0)
                    {
                        StringBuilder stringBuf = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id) values(?,?)");
                        int countSum = DAOUtil.executeUpdate(stringBuf.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
            }
            LOG.debug("成员==============================" + map.get("member_by").toString());
            String memberBy = map.get("member_by").toString();
            if (StringUtils.isNoneBlank(memberBy))
            { // 成员
                String[] idArray = memberBy.split(",");
                if (idArray.length > 0)
                {
                    List<List<Object>> batchValues = new ArrayList<>();
                    for (String menBy : idArray)
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(belogid); // 数据ID
                        model.add(Long.parseLong(menBy)); // 员工id
                        model.add(Constant.CURRENCY_ONE);
                        StringBuilder stringBuf =
                            new StringBuilder("select  count(*) from ").append(manageTable).append(" where file_id = ").append(id).append(" and employee_id=").append(
                                Long.parseLong(menBy));
                        int sum = DAOUtil.executeCount(stringBuf.toString());
                        if (sum <= 0) // 是否存在
                        {
                            batchValues.add(model);
                        }
                    }
                    StringBuilder insertbuf = new StringBuilder("insert into ").append(settingTable).append(" (file_id,employee_id,preview) values(?,?,?)");
                    int countSum = DAOUtil.executeUpdate(insertbuf.toString(), batchValues, 100000); // 权限设置
                    if (countSum <= 0)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                }
            }
            LOG.debug("进入创建文件夹==============================");
            boolean flag = OSSUtil.getInstance().addFolder(Constant.FLIE_LIBRARY_NAME, buff.toString()); // oss创建目录
            LOG.debug("创建成功==============================" + flag);
            if (!flag)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除文件夹
     * 
     */
    @Override
    public ServiceResult<String> delFileLibrary(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            String coverTable = DAOUtil.getTableName("catalog_cover", info.getCompanyId());
            String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
            StringBuilder stringBuf = new StringBuilder("select * from  ").append(catalogTable).append(" where id = ").append(map.get("id"));
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(stringBuf.toString());
            if (null == jsonObject)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            StringBuilder editBuf =
                new StringBuilder("update ").append(catalogTable).append("  set  status = ").append(Constant.CURRENCY_ONE).append(" where id = ").append(map.get("id"));
            int sum = DAOUtil.executeUpdate(editBuf.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String catalogIds = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), Constant.CURRENCY_ONE, "catalog");
            if (!"".equals(catalogIds) && null != catalogIds)
            {// 是否存在子级 存在则删除
                StringBuilder updateBuf = new StringBuilder("update ").append(catalogTable)
                    .append("  set  status = ")
                    .append(Constant.CURRENCY_ONE)
                    .append(" where id in (")
                    .append(map.get("id"))
                    .append(")");
                int count = DAOUtil.executeUpdate(updateBuf.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            
            StringBuilder delBuf = new StringBuilder("delete from ").append(coverTable).append(" where file_id=").append(map.get("id"));
            int num = DAOUtil.executeUpdate(delBuf.toString());
            LOG.debug("删除文件" + num);
            StringBuilder deleteBuf = new StringBuilder("delete from ").append(shareTable).append(" where file_id=").append(map.get("id"));
            int number = DAOUtil.executeUpdate(deleteBuf.toString());
            LOG.debug("删除文件" + number);
            boolean flag = OSSUtil.getInstance().deleteObject(Constant.FLIE_LIBRARY_NAME, jsonObject.getString("url")); // delLibraryFile(jsonObject.getString("url"));
            if (!flag)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 复制
     * 
     */
    @Override
    public ServiceResult<String> copyFileLibrary(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        try
        {
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            StringBuilder stringBuf =
                new StringBuilder("select count(*) from ").append(manageTable).append(" where file_id =").append(map.get("current_id")).append(" and employee_id = ").append(
                    info.getEmployeeId());
            int type = Integer.parseInt(map.get("style").toString());
            if (type == 1 || type == 2 || type == 4 || type == 5)
            {
                int count = DAOUtil.executeCount(stringBuf.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("postprocess.file.library.error"), resultCode.getMsgZh("postprocess.file.library.error"));
                    return serviceResult;
                }
            }
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            StringBuilder buf = new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(map.get("worn_id"));
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(buf.toString()); // 复制信息
            
            String url = jsonObject.getString("url");
            StringBuilder buff = new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(map.get("current_id"));
            JSONObject json = DAOUtil.executeQuery4FirstJSON(buff.toString()); // 当前信息
            String furl = json.getString("url");
            if (url.indexOf(".") > -1)
            {
                String bean = url.substring(url.lastIndexOf("/") + 1, url.indexOf("."));
                // 拆分url 获取后缀
                String suffix = url.substring(url.indexOf("."), url.length());
                furl = furl + bean + suffix;
            }
            else
            {
                String bean = url.substring(0, url.lastIndexOf("/"));
                String suffix = bean.substring(bean.lastIndexOf("/") + 1, bean.length());
                furl = furl + suffix + "/";
            }
            Long parentId = Long.parseLong(map.get("current_id").toString());
            StringBuilder builder = new StringBuilder();
            // 添加数据记录
            builder.append("insert into ")
                .append(catalogTable)
                .append("(name,url,parent_id,size,siffix,color,table_id,sign,create_by,create_time) values('")
                .append(jsonObject.getString("name"))
                .append("','")
                .append(furl)
                .append("','")
                .append(parentId)
                .append("',")
                .append(jsonObject.getString("size"))
                .append(",'")
                .append(jsonObject.getString("siffix"))
                .append("','")
                .append(jsonObject.getString("color"))
                .append("',")
                .append(jsonObject.getString("table_id"))
                .append(",")
                .append(jsonObject.getString("sign"))
                .append(",")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int sum = DAOUtil.executeUpdate(builder.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            Long currId = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
            StringBuilder insertBuilder = new StringBuilder();
            // 添加版本记录
            insertBuilder.append("insert into catalog_version_")
                .append(info.getCompanyId())
                .append("(file_id,url,name,size,suffix,midf_by,midf_time) values (")
                .append(currId)
                .append(",'")
                .append(furl)
                .append("','")
                .append(jsonObject.getString("name"))
                .append("',")
                .append(jsonObject.getString("size"))
                .append(",'")
                .append(jsonObject.getString("siffix"))
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int count = DAOUtil.executeUpdate(insertBuilder.toString());
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            boolean flag = OSSUtil.getInstance().copyObject(Constant.FLIE_LIBRARY_NAME, url, Constant.FLIE_LIBRARY_NAME, furl);
            // boolean flag = OSSUtil.getInstance().copyLibraryFile(url, furl);
            if (!flag)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
        
    }
    
    /**
     * 转移
     * 
     */
    @Override
    public ServiceResult<String> shiftFileLibrary(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            int type = Integer.parseInt(map.get("style").toString());
            if (type == 1 || type == 2 || type == 4 || type == 5)
            { // worn_id
                StringBuilder buff =
                    new StringBuilder("select count(*) from ").append(manageTable).append(" where file_id =").append(map.get("current_id")).append(" and employee_id = ").append(
                        info.getEmployeeId());
                int count = DAOUtil.executeCount(buff.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("postprocess.file.library.error"), resultCode.getMsgZh("postprocess.file.library.error"));
                    return serviceResult;
                }
            }
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            StringBuilder buf = new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(map.get("worn_id")); // current_id
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(buf.toString());// 复制信息
            StringBuilder builders = new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(map.get("current_id"));
            JSONObject json = DAOUtil.executeQuery4FirstJSON(builders.toString()); // 当前信息
            String url = jsonObject.getString("url");
            String furl = json.getString("url");
            if (url.indexOf(".") > -1)
            {
                String bean = url.substring(url.lastIndexOf("/") + 1, url.indexOf("."));
                // 拆分url 获取后缀
                
                String suffix = url.substring(url.indexOf("."), url.length());
                furl = furl + bean + suffix;
            }
            else
            {
                String bean = url.substring(0, url.lastIndexOf("/"));
                String suffix = bean.substring(bean.lastIndexOf("/") + 1, bean.length());
                furl = furl + suffix + "/";
            }
            // 移动的位置
            Long parentId = Long.parseLong(map.get("current_id").toString());
            StringBuilder buil = new StringBuilder("update ").append(catalogTable)
                .append(" set url = '")
                .append(furl)
                .append("',table_id=")
                .append(json.getString("table_id"))
                .append(",parent_id=")
                .append(parentId)
                .append(",create_by=")
                .append(info.getEmployeeId())
                .append(",create_time = ")
                .append(System.currentTimeMillis())
                .append(" where id = ")
                .append(map.get("worn_id"));
            
            int sum = DAOUtil.executeUpdate(buil.toString());
            if (sum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            String id;
            StringBuilder builder = new StringBuilder();
            String sb = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("worn_id").toString()), 1, "catalog");
            if (!"".equals(sb))
            {
                id = builder.append(sb).append("," + Long.parseLong(map.get("worn_id").toString())).toString();
            }
            else
            {
                id = map.get("worn_id").toString();
            }
            
            StringBuilder delbuilder = new StringBuilder("delete from  ").append(manageTable).append(" where  file_id in (").append(id).append(")");
            int count = DAOUtil.executeUpdate(delbuilder.toString());
            LOG.info("删除管理员" + count);
            StringBuffer queryBuilder = new StringBuffer("select * from  ").append(manageTable).append(" where file_id =").append(map.get("current_id"));
            List<JSONObject> objectList = DAOUtil.executeQuery4JSON(queryBuilder.toString());
            String[] ArrayList = id.split(",");
            List<List<Object>> batchValues = new ArrayList<>();
            if (ArrayList.length > 0)
            {
                for (String menBy : ArrayList)
                {
                    for (int i = 0; i < objectList.size(); i++)
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(Long.parseLong(menBy)); // 数据ID
                        model.add(objectList.get(i).getLong("employee_id")); // 员工id
                        model.add(Constant.CURRENCY_ONE);
                        batchValues.add(model);
                    }
                }
            }
            if (batchValues.size() > 0)
            {
                StringBuilder saveBuilder = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id,sign_type) values(?,?,?)");
                int countSum = DAOUtil.executeUpdate(saveBuilder.toString(), batchValues, 100000); // 权限设置
                if (countSum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            
            boolean flag = OSSUtil.getInstance().copyObject(Constant.FLIE_LIBRARY_NAME, url, Constant.FLIE_LIBRARY_NAME, furl); // .copyLibraryFile(url,
                                                                                                                                // furl);
            if (!flag)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 根目录列表
     * 
     */
    @Override
    public JSONObject queryFileList(Map<String, Object> map)
    {
        int style = Integer.parseInt(map.get("style").toString());
        JSONObject result = new JSONObject();
        switch (style) // 列表 1 公司文件 2 应用文件 3 个人文件 4 我共享的 5 与我共享
        {
            case 1:
                result = queryCompanyList(map);
                break;
            case 2:
                result = queryApplyList(map);
                break;
            case 3:
                result = queryPersonalList(map);
                break;
            case 4:
                result = shareFileList(map);
                break;
            case 5:
                result = shareFileList(map);
                break;
            default:
                break;
        }
        return result;
        
    }
    
    /**
     * 公司文件根目录列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryCompanyList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String belongTable = DAOUtil.getTableName("catalog_belong", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
        String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append(" select c.*,cmf.manager_flag,sef.set_flag,cb.type,sef.upload,sef.download,sef.preview,e.employee_name  from ")
            .append(catalogTable)
            .append(" c  left join ")
            .append(employeeTable)
            .append(" e on c.create_by=e.id left join ")
            .append(belongTable)
            .append(" cb on   c.id = cb.file_id  left join  (select count(*)as manager_flag,file_id from ")
            .append(manageTable)
            .append("  where employee_id=")
            .append(info.getEmployeeId())
            .append("  group by file_id")
            .append(" )cmf on c.id = cmf.file_id left join ( select count(*)as set_flag ,file_id,upload,download,preview from ")
            .append(settingTable)
            .append("   where employee_id=")
            .append(info.getEmployeeId())
            .append("  group by file_id,upload,download,preview )sef  on c.id = sef.file_id     where   c.status = 0 and  c.table_id = 1 and c.parent_id is null ")
            .append(" and (cmf.manager_flag is not null or  sef.set_flag  is not null)");
        if (Integer.parseInt(map.get("sign").toString()) < 2)
        {
            queryBuilder.append("and c.sign = ").append(Constant.CURRENCY_ZERO);
        }
        queryBuilder.append(" order by c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        List<JSONObject> pageDataList = BusinessDAOUtil.getTableDataListPage(queryBuilder.toString(), pageSize, pageNum);
        if (pageDataList.size() > 0)
        {
            for (int i = 0; i < pageDataList.size(); i++)
            { // 拼权限参数
                JSONObject jsonObject = pageDataList.get(i);
                if (jsonObject.getInteger("type") == 0 || jsonObject.getInteger("type") == 1)
                {
                    if (!"".equals(jsonObject.getString("manager_flag")))
                    {
                        jsonObject.put("upload", Constant.CURRENCY_ONE);
                        jsonObject.put("download", Constant.CURRENCY_ONE);
                        jsonObject.put("preview", Constant.CURRENCY_ONE);
                        jsonObject.put("is_manage", Constant.CURRENCY_ONE);
                        continue;
                    }
                    else if (!"".equals(jsonObject.getString("set_flag")))
                    {
                        jsonObject.put("upload", jsonObject.get("upload"));
                        jsonObject.put("download", jsonObject.get("download"));
                        jsonObject.put("preview", jsonObject.get("preview"));
                        jsonObject.put("is_manage", Constant.CURRENCY_ZERO);
                        continue;
                    }
                    else
                    {
                        pageDataList.remove(jsonObject);
                    }
                    
                }
            }
        }
        List<Map<String, Object>> allDataList = null;
        JSONObject pageJson = new JSONObject();
        int totalRows = 0;
        // 获取总数量
        if (StringUtil.isEmpty(queryBuilder.toString()))
        {
            totalRows = 0;
        }
        else
        {
            allDataList = DAOUtil.executeQuery(queryBuilder.toString());
            totalRows = allDataList.size();
        }
        
        // 分页信息
        Integer totalPages = (int)(totalRows / pageSize);
        if (totalRows % pageSize > 0)
        {
            totalPages++;
        }
        pageJson.put("pageSize", pageSize);// 当前数量
        pageJson.put("pageNum", pageNum);// 当前页数
        pageJson.put("totalRows", totalRows);// 总记录数
        pageJson.put("totalPages", totalPages);// 总页数
        pageJson.put("curPageSize", totalRows);// 当前页记录数
        
        result.put("dataList", pageDataList);// 数据列表
        result.put("pageInfo", pageJson);// 分页信息
        return result;
    }
    
    /**
     * 个人跟目录列表
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryPersonalList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select  c.*,e.employee_name from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id   where  c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id is null and c.create_by = ")
            .append(info.getEmployeeId());
        if (Integer.parseInt(map.get("sign").toString()) < 2)
        {
            queryBuilder.append("and c.sign = ").append(Constant.CURRENCY_ZERO);
        }
        queryBuilder.append(" order by c.sign,c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 子级目录列表
     * 
     * @param map
     * @return
     */
    @Override
    public JSONObject queryFilePartList(Map<String, Object> map)
    {
        int style = Integer.parseInt(map.get("style").toString());
        JSONObject result = new JSONObject();
        switch (style) // 列表 1 公司文件 2 应用文件 3 个人文件 4 我共享的 5 与我共享
        {
            case 1:
                result = queryCompanyPartList(map);
                break;
            case 2:
                result = queryPartApplyList(map);
                break;
            case 3:
                result = queryPersonalPartList(map);
                break;
            case 4:
                result = queryPartList(map);
                break;
            case 5:
                result = queryPartList(map);
                break;
            default:
                break;
        }
        return result;
    }
    
    /**
     * 应用文件子级目录
     * 
     * @param map
     * @return
     * @Description:
     */
    private JSONObject queryPartApplyList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.*,e.employee_name,e.picture from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where   c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id =")
            .append(map.get("applicationId"))
            .append("and c.sign = ")
            .append(Integer.parseInt(map.get("sign").toString()))
            .append(" order by c.sign,c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 公司子级目录
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryCompanyPartList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.*,e.employee_name,e.picture,manager_flag from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id left join  (select count(*) as manager_flag,file_id from ")
            .append(manageTable)
            .append("  where employee_id=")
            .append(info.getEmployeeId())
            .append("  group by file_id ) cmf on c.id = cmf.file_id where   c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id =")
            .append(map.get("applicationId"));
        if (Integer.parseInt(map.get("sign").toString()) < 2)
        {
            queryBuilder.append("and c.sign = ").append(Constant.CURRENCY_ZERO);
        }
        if (Integer.parseInt(map.get("style").toString()) == 3)
        {
            queryBuilder.append(" and c.create_by=").append(info.getEmployeeId());
        }
        queryBuilder.append(" order by c.sign,c.create_time desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        List<JSONObject> pageDataList = BusinessDAOUtil.getTableDataListPage(queryBuilder.toString(), pageSize, pageNum);
        if (pageDataList.size() > 0)
        {
            for (int i = 0; i < pageDataList.size(); i++)
            { // 拼权限参数
                JSONObject jsonObject = pageDataList.get(i);
                if ("".equals(jsonObject.get("manager_flag")))
                {
                    jsonObject.put("is_manage", Constant.CURRENCY_ZERO);
                }
                else
                {
                    jsonObject.put("is_manage", Constant.CURRENCY_ONE);
                }
            }
        }
        List<Map<String, Object>> allDataList = null;
        JSONObject pageJson = new JSONObject();
        int totalRows = 0;
        // 获取总数量
        if (StringUtil.isEmpty(queryBuilder.toString()))
        {
            totalRows = 0;
        }
        else
        {
            allDataList = DAOUtil.executeQuery(queryBuilder.toString());
            totalRows = allDataList.size();
        }
        
        // 分页信息
        Integer totalPages = (int)(totalRows / pageSize);
        if (totalRows % pageSize > 0)
        {
            totalPages++;
        }
        pageJson.put("pageSize", pageSize);// 当前数量
        pageJson.put("pageNum", pageNum);// 当前页数
        pageJson.put("totalRows", totalRows);// 总记录数
        pageJson.put("totalPages", totalPages);// 总页数
        pageJson.put("curPageSize", totalRows);// 当前页记录数
        
        result.put("dataList", pageDataList);// 数据列表
        result.put("pageInfo", pageJson);// 分页信息
        return result;
    }
    
    /**
     * 个人文件子级目录
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryPersonalPartList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
        StringBuilder queryCountBuilder = new StringBuilder();
        queryCountBuilder.append(" select (1) from ");
        queryCountBuilder.append(shareTable);
        queryCountBuilder.append(" where file_id =  ");
        queryCountBuilder.append(map.get("applicationId"));
        queryCountBuilder.append(" and share_by = ");
        queryCountBuilder.append(info.getEmployeeId());
        int count = DAOUtil.executeCount(queryCountBuilder.toString());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.*,e.employee_name,e.picture from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where   c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id =")
            .append(map.get("applicationId"));
        if (count <= 0)
        {
            queryBuilder.append(" and c.create_by=").append(info.getEmployeeId());
        }
        if (Integer.parseInt(map.get("sign").toString()) < 2)
        {
            queryBuilder.append("and c.sign = ").append(Constant.CURRENCY_ZERO);
        }
        queryBuilder.append(" order by c.sign,c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 我共享的 共享给我的子级目录
     * 
     * @param map
     * @return
     * @Description:
     */
    public JSONObject queryPartList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.*,e.employee_name,e.picture from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where   c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id =")
            .append(map.get("applicationId"));
        if (Integer.parseInt(map.get("sign").toString()) < 2)
        {
            queryBuilder.append("and c.sign = ").append(Constant.CURRENCY_ZERO);
        }
        queryBuilder.append(" order by c.sign,c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 查询文件库详情
     * 
     */
    @Override
    public JSONObject queryFolderDetail(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        StringBuilder queryBuilder =
            new StringBuilder("select * from ").append(catalogTable).append(" where table_id = ").append(map.get("type")).append(" and id = ").append(map.get("id"));
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        return jsonObject;
    }
    
    /**
     * 修改文件夹管理
     * 
     */
    @Override
    public ServiceResult<String> editFolder(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            StringBuilder queryBuilder = new StringBuilder("update ").append(catalogTable)
                .append(" set name = '")
                .append(map.get("name"))
                .append("',color='")
                .append(map.get("color"))
                .append("' where id = ")
                .append(map.get("id"));
            int count = DAOUtil.executeUpdate(queryBuilder.toString());
            if (count <= 0)
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
     * 重命名
     * 
     */
    @Override
    public ServiceResult<String> editRename(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String versionTable = DAOUtil.getTableName("catalog_version", info.getCompanyId());
        StringBuilder queryBuilder =
            new StringBuilder("update ").append(catalogTable).append(" set name = '").append(map.get("name")).append("' where id = ").append(map.get("id"));
        int count = DAOUtil.executeUpdate(queryBuilder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder queryBuil = new StringBuilder("update ").append(versionTable)
            .append(" set name = '")
            .append(map.get("name"))
            .append("' where id = (SELECT id FROM ")
            .append(versionTable)
            .append(" where file_id = ")
            .append(map.get("id"))
            .append(" order by id desc LIMIT 1)");
        int sum = DAOUtil.executeUpdate(queryBuil.toString());
        if (sum <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取管理员设置信息
     * 
     */
    @Override
    public JSONObject queryFolderInitDetail(Map<String, Object> map)
    {
        JSONObject jsonObject = null;
        try
        {
            jsonObject = new JSONObject();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            String belongTable = DAOUtil.getTableName("catalog_belong", info.getCompanyId());
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select c.name,t.type from ")
                .append(catalogTable)
                .append(" c left join ")
                .append(belongTable)
                .append(" t on c.id = t.file_id   where c.id = ")
                .append(map.get("id"))
                .append(" and  c.status = ")
                .append(Constant.CURRENCY_ZERO);
            jsonObject.put("basics", DAOUtil.executeQuery4FirstJSON(queryBuilder.toString())); // 基础信息
            StringBuilder builder = new StringBuilder("select m.*,e.employee_name,e.picture from  ").append(manageTable)
                .append(" m left join ")
                .append(employeeTable)
                .append(" e on m.employee_id = e.id where  file_id=")
                .append(map.get("id"));
            jsonObject.put("manage", DAOUtil.executeQuery4JSON(builder.toString())); // 管理员信息
            StringBuilder buff = new StringBuilder();
            buff.append("select s.*,e.employee_name,e.picture from  ")
                .append(settingTable)
                .append(" s left join ")
                .append(employeeTable)
                .append(" e on s.employee_id = e.id where  s.file_id=")
                .append(map.get("id"))
                .append(" and s.sign = ")
                .append(Constant.CURRENCY_ZERO); // 成员信息
            jsonObject.put("setting", DAOUtil.executeQuery4JSON(buff.toString()));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        
        return jsonObject;
    }
    
    /**
     * 添加管理员
     * 
     */
    @Override
    public ServiceResult<String> savaManageStaff(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select count(1) from ");
            queryBuilder.append(manageTable);
            queryBuilder.append(" where file_id = ");
            queryBuilder.append(map.get("id"));
            queryBuilder.append(" and employee_id = ");
            queryBuilder.append(info.getEmployeeId());
            // 查询是否有权限操作
            int number = DAOUtil.executeCount(queryBuilder.toString());
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.file.handle.library.error"), resultCode.getMsgZh("postprocess.file.handle.library.error"));
                return serviceResult;
            }
            
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append("select count(1) from ");
            queryCountBuilder.append(manageTable);
            queryCountBuilder.append(" where file_id = ");
            queryCountBuilder.append(map.get("id"));
            queryCountBuilder.append(" and employee_id in (");
            queryCountBuilder.append(map.get("manageId"));
            queryCountBuilder.append(")");
            int numberCount = DAOUtil.executeCount(queryCountBuilder.toString());
            if (numberCount > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "添加管理员有重复");
                return serviceResult;
            }
            
            String[] idArray = map.get("manageId").toString().split(",");
            if (idArray.length > 0)
            {
                List<List<Object>> batchValues = new ArrayList<>();
                for (String menBy : idArray)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(Long.parseLong(map.get("id").toString())); // 数据ID
                    model.add(Long.parseLong(menBy)); // 员工id
                    StringBuilder buff = new StringBuilder("select  count(*) from ").append(manageTable)
                        .append(" where file_id = ")
                        .append(map.get("id").toString())
                        .append(" and employee_id=")
                        .append(Long.parseLong(menBy));
                    int count = DAOUtil.executeCount(buff.toString());
                    if (count <= 0)
                    {
                        batchValues.add(model);
                    }
                }
                if (batchValues.size() > 0)
                {
                    StringBuilder savaBuff = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id) values(?,?)");
                    int countSum = DAOUtil.executeUpdate(savaBuff.toString(), batchValues, 100000); // 权限设置
                    if (countSum <= 0)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                }
            }
            String catalogIds = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), Constant.CURRENCY_ONE, "catalog"); // 是否存在子级
            // 存在则把相应的删除
            if (!"".equals(catalogIds) && null != catalogIds)
            {
                String[] ArrayList = catalogIds.split(",");
                if (ArrayList.length > 0)
                {
                    List<List<Object>> batchValues = new ArrayList<>();
                    for (String menBy : idArray)
                    {
                        for (String perId : ArrayList)
                        {
                            List<Object> model = new ArrayList<>();
                            model.add(Long.parseLong(perId)); // 数据ID
                            model.add(Long.parseLong(menBy)); // 员工id
                            model.add(Constant.CURRENCY_ONE); //
                            StringBuilder builder = new StringBuilder("select  count(*) from ").append(manageTable)
                                .append(" where file_id = ")
                                .append(Long.parseLong(perId))
                                .append(" and employee_id=")
                                .append(Long.parseLong(menBy));
                            int count = DAOUtil.executeCount(builder.toString());
                            if (count <= 0)
                            {
                                batchValues.add(model);
                            }
                        }
                    }
                    if (batchValues.size() > 0)
                    {
                        StringBuilder savabuilder = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id,sign_type) values(?,?,?)");
                        int countSum = DAOUtil.executeUpdate(savabuilder.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
            }
            if (null != map.get("sign") && !"".equals(map.get("sign")))
            {
                StringBuilder delBuilder = new StringBuilder("delete from ").append(settingTable)
                    .append(" where file_id=")
                    .append(map.get("id"))
                    .append(" and employee_id in (")
                    .append(map.get("manageId"))
                    .append(")");
                int count = DAOUtil.executeUpdate(delBuilder.toString());// 删除
                LOG.debug("添加管理员" + count);
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 根目录设置权限
     * 
     */
    @Override
    public ServiceResult<String> updateSetting(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            JSONObject layoutJson = JSONObject.parseObject(map.get("reqJsonStr").toString());
            JSONArray jsonArray = JSONArray.parseArray(layoutJson.getString("data"));
            List<List<Object>> batchValues = new ArrayList<>();
            
            for (int i = 0; i < jsonArray.size(); i++)
            {
                JSONObject object = (JSONObject)jsonArray.get(i);
                List<Object> model = new ArrayList<>();
                model.add(object.getInteger("upload")); // 数据ID
                model.add(object.getInteger("download")); // 员工id
                model.add(object.getInteger("preview"));
                model.add(object.getLong("id"));
                batchValues.add(model);
            }
            StringBuilder editBuilder = new StringBuilder("update  ").append(settingTable).append(" set upload = ?,download=?,preview=? where id=? ");
            int countSum = DAOUtil.executeUpdate(editBuilder.toString(), batchValues, 100000); // 权限设置
            if (countSum <= 0)
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
     * 获取菜单
     * 
     */
    @Override
    public List<JSONObject> queryfileCatalog(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String tableTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder("select * from ").append(tableTable).append(" where status =").append(Constant.CURRENCY_ONE).append(" order by id asc");
        return DAOUtil.executeQuery4JSON(queryBuilder.toString());
    }
    
    /**
     * 获取管理员
     * 
     */
    @Override
    public List<JSONObject> queryManageById(String id, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder(" select m.*,e.employee_name,e.picture from ").append(manageTable)
            .append(" m left join ")
            .append(employeeTable)
            .append(" e on m.employee_id=e.id  where m.file_id=")
            .append(id);
        return DAOUtil.executeQuery4JSON(queryBuilder.toString());
    }
    
    /**
     * 共享文件夹
     * 
     */
    @Override
    public ServiceResult<String> shareFileLibaray(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String coverTable = DAOUtil.getTableName("catalog_cover", info.getCompanyId());
            String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
            StringBuilder savaBuilder =
                new StringBuilder("insert into ").append(coverTable).append(" (file_id,cover_by) values(").append(map.get("id")).append(",").append(info.getEmployeeId()).append(
                    ")");
            int countSum = DAOUtil.executeUpdate(savaBuilder.toString());
            if (countSum <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            Long id = BusinessDAOUtil.geCurrval4Table("catalog_cover", info.getCompanyId().toString());
            String[] idArray = map.get("shareBy").toString().split(",");
            if (idArray.length > 0)
            {
                List<List<Object>> batchValues = new ArrayList<>();
                for (String shareBy : idArray)
                {
                    List<Object> model = new ArrayList<>();
                    model.add(Long.parseLong(map.get("id").toString())); // 数据ID
                    model.add(id); // 共享ID
                    model.add(Long.parseLong(shareBy)); // 被共享人
                    batchValues.add(model);
                }
                StringBuilder addBuilder = new StringBuilder("insert into ").append(shareTable).append(" (file_id,cover_id,share_by) values(?,?,?)");
                int sum = DAOUtil.executeUpdate(addBuilder.toString(), batchValues, 100000); // 权限设置
                if (sum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 我共享的 共享给我的根目录列表
     */
    @Override
    public JSONObject shareFileList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        StringBuilder addBuilder = new StringBuilder();
        String sql = commonSql(Integer.parseInt(map.get("style").toString()), info);
        addBuilder.append(sql);
        if (Integer.parseInt(map.get("sign").toString()) < 2)
        {
            addBuilder.append(" and c.sign = ").append(Constant.CURRENCY_ZERO);
        }
        addBuilder.append(" order by c.sign,c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(addBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 我共享的 共享给我的语句
     * 
     * @param parseInt
     * @param info
     * @return
     * @Description:
     */
    private String commonSql(int parseInt, InfoVo info)
    {
        StringBuilder queryBuilder = new StringBuilder();
        String coverTable = DAOUtil.getTableName("catalog_cover", info.getCompanyId());
        String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        switch (parseInt)
        {
            case 4:
                queryBuilder.append("select  c.id,c.size,c.name,c.parent_id,c.table_id table_id,c.sign,c.create_time,c.siffix,c.status,c.color,e.employee_name,s.id file_id from ")
                    .append(catalogTable)
                    .append(" c left join ")
                    .append(coverTable)
                    .append(" s  on c.id=s.file_id left join ")
                    .append(employeeTable)
                    .append("  e ON s.cover_by = e. ID    where s.cover_by=")
                    .append(info.getEmployeeId())
                    .append(" and s.cover_status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" and  c.status = ")
                    .append(Constant.CURRENCY_ZERO);
                break;
            case 5:
                queryBuilder.append("select  c.id,c.size,c.name,c.parent_id,c.table_id table_id,c.sign,c.create_time,c.siffix,c.status,c.color,e.employee_name,s.id file_id  from ")
                    .append(catalogTable)
                    .append(" c left join ")
                    .append(shareTable)
                    .append(" s  on c.id=s.file_id   left join ")
                    .append(coverTable)
                    .append(" v on s.cover_id = v.id left join ")
                    .append(employeeTable)
                    .append(" e ON v.cover_by = e. ID      where s.share_by=")
                    .append(info.getEmployeeId())
                    .append(" and s.share_status = ")
                    .append(Constant.CURRENCY_ZERO)
                    .append(" and  c.status = ")
                    .append(Constant.CURRENCY_ZERO);
                break;
            default:
                break;
        }
        return queryBuilder.toString();
    }
    
    /**
     * 取消共享
     * 
     */
    @Override
    public ServiceResult<String> cancelShare(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String coverTable = DAOUtil.getTableName("catalog_cover", info.getCompanyId());
        String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
        StringBuilder editBuilder =
            new StringBuilder("update ").append(coverTable).append(" set cover_status = '").append(Constant.CURRENCY_ONE).append("' where id = ").append(map.get("id"));
        int count = DAOUtil.executeUpdate(editBuilder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        StringBuilder updateBuilder =
            new StringBuilder("update ").append(shareTable).append(" set  share_status = ").append(Constant.CURRENCY_ONE).append(" where  cover_id= ").append(map.get("id"));
        int sum = DAOUtil.executeUpdate(updateBuilder.toString());
        if (sum <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 退出共享
     * 
     */
    @Override
    public ServiceResult<String> quitShare(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
            StringBuilder editBuilder =
                new StringBuilder("update ").append(shareTable).append(" set share_status = ").append(Constant.CURRENCY_ONE).append(" where id = ").append(map.get("id"));
            int count = DAOUtil.executeUpdate(editBuilder.toString());
            if (count <= 0)
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
     * 查看下载记录
     * 
     */
    @Override
    public List<JSONObject> queryDownLoadList(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String downloadTable = DAOUtil.getTableName("download_record", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder("select d.*,e.employee_name,e.picture from ").append(downloadTable)
            .append(" d left join ")
            .append(employeeTable)
            .append(" e on d.employee_id = e.id where d.file_id=")
            .append(map.get("id"))
            .append(" order by d.lately_time desc");
        List<JSONObject> josnObject = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        return josnObject;
    }
    
    /**
     * 查看历史版本记录
     * 
     */
    @Override
    public List<JSONObject> queryVersionList(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String versionTable = DAOUtil.getTableName("catalog_version", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select v.*,e.employee_name,e.picture from ")
            .append(versionTable)
            .append(" v left join ")
            .append(employeeTable)
            .append(" e on v.midf_by = e.id where v.file_id=")
            .append(map.get("id"))
            .append(" order by midf_time desc");
        List<JSONObject> josnObject = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        return josnObject;
    }
    
    /**
     * 赞或取消
     * 
     */
    @Override
    public ServiceResult<String> whetherFabulous(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            StringBuilder builder = new StringBuilder();
            String fabulousTable = DAOUtil.getTableName("fabulous_center", info.getCompanyId());
            int sum = 0;
            if (Integer.parseInt(map.get("status").toString()) == 1)
            {
                StringBuilder queryBuilder = new StringBuilder("select count(*) from   ").append(fabulousTable)
                    .append("  where file_id=")
                    .append(map.get("id"))
                    .append(" and employee_id=")
                    .append(info.getEmployeeId())
                    .append(" and status=")
                    .append(Constant.CURRENCY_ONE);
                sum = DAOUtil.executeCount(queryBuilder.toString());
                if (sum <= 0)
                {
                    builder.append("insert into ")
                        .append(fabulousTable)
                        .append(" (file_id,employee_id) values(")
                        .append(map.get("id"))
                        .append(",")
                        .append(info.getEmployeeId())
                        .append(")");
                }
            }
            else if (Integer.parseInt(map.get("status").toString()) == 0)
            {
                builder.append("update  ")
                    .append(fabulousTable)
                    .append(" set status='")
                    .append(Constant.CURRENCY_ZERO)
                    .append("' where file_id=")
                    .append(map.get("id"))
                    .append(" and employee_id=")
                    .append(info.getEmployeeId());
            }
            if (Constant.CURRENCY_ZERO != builder.length())
            {
                int count = DAOUtil.executeUpdate(builder.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 获取文件信息
     * 
     */
    @Override
    public JSONObject queryFileLibarayDetail(Map<String, Object> map)
    {
        JSONObject jsonObject = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String commentTable = DAOUtil.getTableName("comment", info.getCompanyId());
        String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
        String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
        String fabulousTable = DAOUtil.getTableName("fabulous_center", info.getCompanyId());
        StringBuilder builder = new StringBuilder("select c.*,e.employee_name  from ").append(catalogTable)
            .append(" c left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where c.id = ")
            .append(map.get("id"));
        JSONObject resultJsonObject = DAOUtil.executeQuery4FirstJSON(builder.toString());
        jsonObject.put("basics", resultJsonObject);
        // 点赞数量
        StringBuilder builderCount =
            new StringBuilder("select count(*) from ").append(fabulousTable).append(" where file_id = ").append(map.get("id")).append(" and status = ").append(
                Constant.CURRENCY_ONE);
        int count = DAOUtil.executeCount(builderCount.toString());
        jsonObject.put("fabulous_count", count);
        // 点赞状态
        StringBuilder builderSum = new StringBuilder("select count(*) from ").append(fabulousTable)
            .append(" where file_id = ")
            .append(map.get("id"))
            .append(" and employee_id = ")
            .append(info.getEmployeeId())
            .append(" and status = ")
            .append(Constant.CURRENCY_ONE);
        int sum = DAOUtil.executeCount(builderSum.toString());
        if (sum <= 0)
        {
            jsonObject.put("fabulous_status", Constant.CURRENCY_ZERO);
        }
        else
        {
            jsonObject.put("fabulous_status", Constant.CURRENCY_ONE);
        }
        StringBuilder countBuilder = new StringBuilder("select count(*) from ").append(manageTable)
            .append(" where file_id = ")
            .append(resultJsonObject.getLong("parent_id"))
            .append(" and employee_id = ")
            .append(info.getEmployeeId());
        int countSum = DAOUtil.executeCount(countBuilder.toString());
        LOG.debug("查询是否是管理员");
        if (countSum <= 0)
        {
            String str = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), 0, "catalog");
            
            String[] id = str.split(",");
            String fileId = id[0];
            // 查询文件夹成员
            StringBuilder queryBuilder =
                new StringBuilder("select * from ").append(settingTable).append(" where file_id = ").append(fileId).append(" and  employee_id = ").append(info.getEmployeeId());
            JSONObject json = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
            if (null == json)
            {
                jsonObject.put("is_manage", Constant.CURRENCY_ZERO);
                jsonObject.put("upload", Constant.CURRENCY_ZERO);
                jsonObject.put("download", Constant.CURRENCY_ZERO);
            }
            else
            {
                jsonObject.put("is_manage", Constant.CURRENCY_ZERO);
                jsonObject.put("upload", json.get("upload"));
                jsonObject.put("download", json.get("download"));
            }
        }
        else
        {
            jsonObject.put("is_manage", Constant.CURRENCY_ONE);
            jsonObject.put("upload", Constant.CURRENCY_ONE);
            jsonObject.put("download", Constant.CURRENCY_ONE);
        }
        // 评论数量统计
        StringBuilder numberBuilder =
            new StringBuilder("select count(*) from ").append(commentTable).append(" where  bean = 'file_library' and relation_id=").append(map.get("id"));
        int num = DAOUtil.executeCount(numberBuilder.toString());
        jsonObject.put("comment_count", num);
        return jsonObject;
    }
    
    /**
     * 
     * @param id
     * @param token
     * @return
     * @Description:模糊查询类型数据
     */
    @Override
    public JSONObject blurSearchFileByPage(Long id, String token, String content, Integer pageSize, Integer pageNum)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder searchBlurQuerySB = new StringBuilder();
        if (id == Constant.LIBRARY_COMPANY_CATALOG || id == Constant.LIBRARY_APPLICATION_CATALOG || id == Constant.LIBRARY_PERSONAL_CATALOG)
        {
            searchBlurQuerySB.append("select * from catalog_").append(companyId).append(" where name like '%").append(content).append("%' and table_id = ").append(id).append(
                " order by id");
        }
        Long employeeId = info.getEmployeeId();
        if (id == Constant.LIBRARY_SHARETO_CATALOG || id == Constant.LIBRARY_TOSHARE_CATALOG)
        {
            searchBlurQuerySB.append("select * from catalog_share_")
                .append(companyId)
                .append(" cs,catalog_")
                .append(companyId)
                .append(" c where cs.file_id = c.ID and c.NAME like '%")
                .append(content);
            if (id == Constant.LIBRARY_SHARETO_CATALOG)
            {
                searchBlurQuerySB.append("%' and cover_by = ").append(employeeId);
            }
            if (id == Constant.LIBRARY_TOSHARE_CATALOG)
            {
                searchBlurQuerySB.append("%' and share_by = ").append(employeeId);
            }
        }
        return BusinessDAOUtil.getTableDataListAndPageInfo(searchBlurQuerySB.toString(), pageSize, pageNum);
    }
    
    /**
     * 
     * @param id
     * @param token
     * @return
     * @Description:获取搜索的文件的父节点信息
     */
    @Override
    public List<JSONObject> getFileBrowerInfo(Long id, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        String catalogIds = BusinessDAOUtil.getFileDepments(companyId, id, Constant.CURRENCY_ZERO, "catalog");
        StringBuilder queryAllCatalog =
            new StringBuilder().append("select id,name,parent_id from catalog_").append(companyId).append(" where id in (").append(catalogIds).append(")");
        return DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
        
    }
    
    /**
     * 
     * @param id
     * @param token
     * @return
     * @Description:模糊查询类型数据
     */
    @Override
    public List<JSONObject> blurSearchFile(Long id, String token, String content, Long fileId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        if (id == Constant.LIBRARY_COMPANY_CATALOG) // 公司文件夹
        {
            if (Objects.isNull(fileId))
            {
                return SearchLibFile.searchCompanyFile(companyId, id, employeeId, content);
            }
            else
            {
                return SearchLibFile.searchCompanyFile(companyId, id, employeeId, content, fileId);
            }
            
        }
        StringBuilder searchBlurQuerySB = new StringBuilder();
        if (id == Constant.LIBRARY_PERSONAL_CATALOG) // 个人文件夹
        {
            searchBlurQuerySB.append("select c.*,e.employee_name from catalog_").append(companyId).append(" c join employee_");
            searchBlurQuerySB.append(companyId).append(" e on c.create_by = e.id where c.name like '%").append(content);
            searchBlurQuerySB.append("%' and c.table_id = ").append(id).append(" and c.create_by =").append(employeeId);
            if (!Objects.isNull(fileId))
            {
                String dataId = BusinessDAOUtil.getFileDepments(companyId, fileId, Constant.CURRENCY_ONE, "catalog");
                searchBlurQuerySB.append(" and c.id in ( ").append(dataId).append(") ");
            }
            searchBlurQuerySB.append(" order by c.id");
        }
        if (id == Constant.LIBRARY_APPLICATION_CATALOG) // 应用文件夹
        {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("token", token);
            jsonObject.put("module_id", fileId);
            String dataId = moduleDataAuthAppService.getModuleDataByAuthModule(jsonObject); // 获取模块下能看到的文件
            if (StringUtils.isEmpty(dataId))
            {
                return null;
            }
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
            searchBlurQuerySB.append("select c.*,e.employee_name,e.picture from  ");
            searchBlurQuerySB.append(catalogTable);
            searchBlurQuerySB.append(" c  left join ");
            searchBlurQuerySB.append(employeeTable);
            searchBlurQuerySB.append(" e on c.create_by = e.id  where   c.status = ");
            searchBlurQuerySB.append(Constant.CURRENCY_ZERO);
            searchBlurQuerySB.append(" and c.table_id =");
            searchBlurQuerySB.append(id);
            searchBlurQuerySB.append("  and c.parent_id =");
            searchBlurQuerySB.append(fileId);
            searchBlurQuerySB.append(" and  model_id in (");
            searchBlurQuerySB.append(dataId);
            searchBlurQuerySB.append(") and c.type = ");
            searchBlurQuerySB.append(Constant.CURRENCY_TWO);
            searchBlurQuerySB.append("  and c.name like '%");
            searchBlurQuerySB.append(content);
            searchBlurQuerySB.append("%' order by c.id asc");
        }
        if (id == Constant.LIBRARY_SHARETO_CATALOG) // 我共享的
        {
            if (Objects.isNull(fileId))
            {
                return SearchLibFile.searchShareToFile(companyId, id, employeeId, content);
            }
            else
            {
                return SearchLibFile.searchShareFile(companyId, id, employeeId, content, fileId);
            }
            
        }
        if (id == Constant.LIBRARY_TOSHARE_CATALOG) // 与我共享
        {
            if (Objects.isNull(fileId))
            {
                return SearchLibFile.searchToShareFile(companyId, id, employeeId, content);
            }
            else
            {
                return SearchLibFile.searchShareFile(companyId, id, employeeId, content, fileId);
            }
            
        }
        return DAOUtil.executeQuery4JSON(searchBlurQuerySB.toString());
    }
    
    /**
     * 是否开启个人
     * 
     */
    @Override
    public ServiceResult<String> updateIsOpen(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String fabulousTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        StringBuilder queryBuilder =
            new StringBuilder("update  ").append(fabulousTable).append(" set status='").append(map.get("status")).append("' where id=").append(Constant.CURRENCY_THREE);
        int count = DAOUtil.executeUpdate(queryBuilder.toString());
        if (count <= 0)
        {
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 后台获取公司列表
     * 
     */
    @Override
    public JSONObject queryCompanyFileList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String belongTable = DAOUtil.getTableName("catalog_belong", info.getCompanyId());
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.id,c.color,c.name,c.create_time,c.size,c.url,c.sign,e.employee_name,b.type from ")
            .append(muTable)
            .append(" t left join ")
            .append(catalogTable)
            .append(" c on t.id = c.table_id left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  left join ")
            .append(belongTable)
            .append(" b on c.id =b.file_id  where  c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(Constant.CURRENCY_ONE)
            .append("  and c.parent_id is null  and c.sign = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" order by c.id desc");
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 获取是否开启个人文件
     * 
     */
    @Override
    public JSONObject queryPersonalStatus(String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        JSONObject result = new JSONObject();
        String muTable = DAOUtil.getTableName("catalog_table", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder("select status from ").append(muTable).append(" where id = ").append(Constant.CURRENCY_THREE);
        Object object = DAOUtil.executeQuery4Object(queryBuilder.toString());
        result.put("status", object);
        return result;
    }
    
    /**
     * 删除管理员
     * 
     */
    @Override
    public ServiceResult<String> delManageStaff(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            StringBuilder delBuilder = new StringBuilder("delete from ").append(manageTable)
                .append(" where file_id=")
                .append(map.get("id"))
                .append(" and sign_type = ")
                .append(Constant.CURRENCY_ZERO)
                .append(" and employee_id = ")
                .append(map.get("manageId"));
            int count = DAOUtil.executeUpdate(delBuilder.toString());// 删除
            if (count <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
            
            String catalogIds = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), Constant.CURRENCY_ONE, "catalog"); // 是否存在子级
            // 存在则把相应的删除
            if (!"".equals(catalogIds) && null != catalogIds)
            {
                StringBuilder deleteBuilder =
                    new StringBuilder("delete from ").append(manageTable).append(" where file_id in (").append(catalogIds).append(")  and employee_id = ").append(
                        map.get("manageId"));
                int sum = DAOUtil.executeUpdate(deleteBuilder.toString()); // 自己跟子级管理员也需删除
                LOG.debug("删除管理员" + sum);
            }
            if (null != map.get("sign"))
            {
                StringBuilder insertBuilder = new StringBuilder("insert into ").append(settingTable)
                    .append(" (file_id,employee_id) values(")
                    .append(map.get("id"))
                    .append(",")
                    .append(map.get("manageId"))
                    .append(")");
                int countSum = DAOUtil.executeUpdate(insertBuilder.toString());// 删除的管理员添加到本级成员
                LOG.debug("添加成員" + countSum);
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 一体添加删除
     * 
     */
    @Override
    public ServiceResult<String> editManageStaff(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            String catalogIds = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), Constant.CURRENCY_ONE, "catalog");
            if (!map.get("manageId").equals("") && null != map.get("manageId"))
            {
                StringBuilder queryBuilder = new StringBuilder();
                queryBuilder.append("select count(1) from ");
                queryBuilder.append(manageTable);
                queryBuilder.append(" where file_id = ");
                queryBuilder.append(map.get("id"));
                queryBuilder.append(" and employee_id = ");
                queryBuilder.append(info.getEmployeeId());
                // 查询是否有权限操作
                int number = DAOUtil.executeCount(queryBuilder.toString());
                if (number <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("postprocess.file.handle.library.error"), resultCode.getMsgZh("postprocess.file.handle.library.error"));
                    return serviceResult;
                }
                
                StringBuilder queryCountBuilder = new StringBuilder();
                queryCountBuilder.append("select count(1) from ");
                queryCountBuilder.append(manageTable);
                queryCountBuilder.append(" where file_id = ");
                queryCountBuilder.append(map.get("id"));
                queryCountBuilder.append(" and employee_id in (");
                queryCountBuilder.append(map.get("manageId"));
                queryCountBuilder.append(")");
                int numberCount = DAOUtil.executeCount(queryCountBuilder.toString());
                if (numberCount > 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), "添加管理员有重复");
                    return serviceResult;
                }
                
                String[] idArray = map.get("manageId").toString().split(",");
                if (idArray.length > 0)
                {
                    List<List<Object>> batchValues = new ArrayList<>();
                    for (String menBy : idArray)
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(Long.parseLong(map.get("id").toString())); // 数据ID
                        model.add(Long.parseLong(menBy)); // 员工id
                        batchValues.add(model);
                    }
                    StringBuilder insertBuilder = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id) values(?,?)");
                    int countSum = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 100000); // 添加管理员
                    if (countSum <= 0)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                    if (null != map.get("sign"))
                    {
                        StringBuilder delBuilder = new StringBuilder("delete from ").append(settingTable)
                            .append(" where file_id=")
                            .append(map.get("id"))
                            .append(" and employee_id in (")
                            .append(map.get("manageId"))
                            .append(")");
                        int count = DAOUtil.executeUpdate(delBuilder.toString());// 删除成员
                        
                    }
                }
                if (!"".equals(catalogIds) && null != catalogIds)
                { // 是否存在子级
                  // 存在则子级都增加此管理员
                    String[] ArrayList = catalogIds.split(",");
                    if (ArrayList.length > 0)
                    {
                        List<List<Object>> batchValues = new ArrayList<>();
                        for (String menBy : idArray)
                        {
                            for (String perId : ArrayList)
                            {
                                List<Object> model = new ArrayList<>();
                                model.add(Long.parseLong(perId)); // 数据ID
                                model.add(Long.parseLong(menBy)); // 员工id
                                model.add(Constant.CURRENCY_ONE); //
                                batchValues.add(model);
                            }
                        }
                        StringBuilder savaBuilder = new StringBuilder("insert into ").append(manageTable).append(" (file_id,employee_id,sign_type) values(?,?,?)");
                        int countSum = DAOUtil.executeUpdate(savaBuilder.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
                
            }
            if (!map.get("deleteId").equals("") && null != map.get("deleteId"))
            { // 是否又删除 又则当前删除
                StringBuilder delBuilder = new StringBuilder("delete from ").append(manageTable)
                    .append(" where file_id=")
                    .append(map.get("id"))
                    .append(" and employee_id in (")
                    .append(map.get("deleteId"))
                    .append(")")
                    .append(" and sign_type = ")
                    .append(Constant.CURRENCY_ZERO);
                int count = DAOUtil.executeUpdate(delBuilder.toString());
                if (count <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
                if (!"".equals(catalogIds) && null != catalogIds)
                {// 是否存在子级
                 // 存在则把相应的删除
                    StringBuilder deleteBuilder = new StringBuilder("delete from ").append(manageTable)
                        .append(" where file_id in (")
                        .append(catalogIds)
                        .append(") and sign_type = ")
                        .append(Constant.CURRENCY_ONE)
                        .append(" and employee_id in (")
                        .append(map.get("deleteId"))
                        .append(")");
                    int sum = DAOUtil.executeUpdate(deleteBuilder.toString());
                    if (sum <= 0)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                    
                }
                if (null != map.get("sign") && !"".equals(map.get("sign")))
                {
                    String[] idArray = map.get("deleteId").toString().split(",");
                    if (idArray.length > 0)
                    {
                        List<List<Object>> batchValues = new ArrayList<>();
                        for (String menBy : idArray)
                        {
                            List<Object> model = new ArrayList<>();
                            model.add(Long.parseLong(map.get("id").toString())); // 数据ID
                            model.add(Long.parseLong(menBy)); // 员工id
                            batchValues.add(model);
                        }
                        StringBuilder savaBuilder = new StringBuilder("insert into ").append(settingTable).append(" (file_id,employee_id) values(?,?)");
                        int countSum = DAOUtil.executeUpdate(savaBuilder.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 添加成员
     * 
     */
    @Override
    public ServiceResult<String> savaMember(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            StringBuilder queryBuilder = new StringBuilder();
            queryBuilder.append("select count(1) from ");
            queryBuilder.append(manageTable);
            queryBuilder.append(" where file_id = ");
            queryBuilder.append(map.get("id"));
            queryBuilder.append(" and employee_id = ");
            queryBuilder.append(info.getEmployeeId());
            // 验证是否有权限
            int number = DAOUtil.executeCount(queryBuilder.toString());
            if (number <= 0)
            {
                serviceResult.setCodeMsg(resultCode.get("postprocess.file.handle.library.error"), resultCode.getMsgZh("postprocess.file.handle.library.error"));
                return serviceResult;
            }
            
            StringBuilder queryCountBuilder = new StringBuilder();
            queryCountBuilder.append(" select * from ");
            queryCountBuilder.append(settingTable);
            queryCountBuilder.append(" where file_id =");
            queryCountBuilder.append(map.get("id"));
            queryCountBuilder.append(" and employee_id in (");
            queryCountBuilder.append(map.get("memberId"));
            queryCountBuilder.append(")");
            // 验证添加是否有重复
            int count = DAOUtil.executeCount(queryCountBuilder.toString());
            if (count > 0)
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), "添加成员有重复");
                return serviceResult;
            }
            
            String memberBy = map.get("memberId").toString();
            if (StringUtils.isNoneBlank(memberBy))
            { // 成员
                String[] idArray = memberBy.split(",");
                if (idArray.length > 0)
                {
                    List<List<Object>> batchValues = new ArrayList<>();
                    for (String menBy : idArray)
                    {
                        List<Object> model = new ArrayList<>();
                        model.add(Long.parseLong(map.get("id").toString())); // 数据ID
                        model.add(Long.parseLong(menBy)); // 员工id
                        batchValues.add(model);
                    }
                    StringBuilder savaBuilder = new StringBuilder("insert into ").append(settingTable).append(" (file_id,employee_id) values(?,?)");
                    int countSum = DAOUtil.executeUpdate(savaBuilder.toString(), batchValues, 100000); // 权限设置
                    if (countSum <= 0)
                    {
                        serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                        return serviceResult;
                    }
                }
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 删除成员
     * 
     */
    @Override
    public ServiceResult<String> delMember(Map<String, Object> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            StringBuilder delBuilder =
                new StringBuilder("delete from ").append(settingTable).append(" where file_id=").append(map.get("id")).append("  and employee_id = ").append(map.get("memberId"));
            int count = DAOUtil.executeUpdate(delBuilder.toString());// 删除
            if (count <= 0)
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
     * web端一体删除成员
     * 
     */
    @Override
    public ServiceResult<String> editMember(Map<String, Object> map)
    {
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            String memberBy = map.get("memberId").toString();
            
            if (!map.get("memberId").equals(""))
            {
                if (StringUtils.isNoneBlank(memberBy))
                { // 成员
                    String[] idArray = memberBy.split(",");
                    if (idArray.length > 0)
                    {
                        List<List<Object>> batchValues = new ArrayList<>();
                        for (String menBy : idArray)
                        {
                            List<Object> model = new ArrayList<>();
                            model.add(Long.parseLong(map.get("id").toString())); // 数据ID
                            model.add(Long.parseLong(menBy)); // 员工id
                            batchValues.add(model);
                        }
                        StringBuilder insertBuilder = new StringBuilder("insert into ").append(settingTable).append(" (file_id,employee_id) values(?,?)");
                        int countSum = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 100000); // 权限设置
                        if (countSum <= 0)
                        {
                            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                            return serviceResult;
                        }
                    }
                }
            }
            if (!map.get("deleteId").equals(""))
            {
                StringBuilder delBuilder = new StringBuilder("delete from ").append(settingTable)
                    .append(" where file_id=")
                    .append(map.get("id"))
                    .append(" and employee_id in (")
                    .append(map.get("deleteId"))
                    .append(")");
                int count = DAOUtil.executeUpdate(delBuilder.toString());// 删除
                LOG.info("删除" + count);
            }
        }
        catch (NumberFormatException e)
        {
            LOG.error(e.getMessage(), e);
            serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
            return serviceResult;
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 小助手获取权限
     * 
     */
    @Override
    public JSONObject queryAidePower(Map<String, Object> map)
    {
        LOG.debug("进入助手查询是否有权限");
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        Integer style = Integer.parseInt(map.get("style").toString());
        JSONObject jsonObject = new JSONObject();
        switch (style)
        {
            case 1:
                jsonObject = commonCompany(map, info);
                break;
            case 2:
                jsonObject = commonApply(map, info);
                break;
            case 3:
                
                break;
            case 4:
                jsonObject = commonCove(map, info);
                break;
            case 5:
                jsonObject = commonShare(map, info);
                break;
            default:
                break;
        }
        
        return jsonObject;
    }
    
    /**
     * 助手共享是否有权限
     * 
     * @param map
     * @param info
     * @return
     * @Description:
     */
    private JSONObject commonShare(Map<String, Object> map, InfoVo info)
    {
        JSONObject resultJsonObject = new JSONObject();
        String shareTable = DAOUtil.getTableName("catalog_share", info.getCompanyId());
        
        StringBuilder builder = new StringBuilder();
        builder.append("select count(*) from ").append(shareTable).append(" where file_id =  ").append(map.get("id"));
        int count = DAOUtil.executeCount(builder.toString());
        if (count <= 0)
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_TWO);
            return resultJsonObject;
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select * from ")
            .append(shareTable)
            .append(" where file_id = ")
            .append(map.get("id"))
            .append(" and share_by = ")
            .append(info.getEmployeeId())
            .append(" and share_status=")
            .append(Constant.CURRENCY_ZERO);
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        if (null == jsonObject)
        {
            //无权限
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
        }
        else
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
        }
        return resultJsonObject;
    }
    
    /**
     * 共享是否有权限
     * 
     * @param map
     * @param info
     * @return
     * @Description:
     */
    private JSONObject commonCove(Map<String, Object> map, InfoVo info)
    {
        JSONObject resultJsonObject = new JSONObject();
        String coverTable = DAOUtil.getTableName("catalog_cover", info.getCompanyId());
        StringBuilder builder = new StringBuilder();
        builder.append("select count(*) from ").append(coverTable).append(" where file_id =  ").append(map.get("id"));
        int count = DAOUtil.executeCount(builder.toString());
        if (count <= 0)
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_TWO);
            return resultJsonObject;
        }
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select * from ")
            .append(coverTable)
            .append(" where file_id = ")
            .append(map.get("id"))
            .append(" and cover_by = ")
            .append(info.getEmployeeId())
            .append(" and cover_status=")
            .append(Constant.CURRENCY_ZERO);
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        if (null == jsonObject)
        {
            //无权限
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
        }
        else
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
        }
        return resultJsonObject;
    }
    
    /**
     * 应用是否有权限
     * 
     * @param map
     * @param info
     * @return
     * @Description:
     */
    private JSONObject commonApply(Map<String, Object> map, InfoVo info)
    {
        JSONObject resultJsonObject = new JSONObject();
        
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select parent_id from  ").append(catalogTable).append(" where   status = ").append(Constant.CURRENCY_ZERO).append("  and id =").append(map.get("id"));
        Object object = DAOUtil.executeQuery4Object(queryBuilder.toString()); // 查询是否存在
        if (null == object)
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_TWO);
            return resultJsonObject;
        }
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("token", map.get("token").toString());
        jsonObject.put("module_id", object);
        String id = moduleDataAuthAppService.getModuleDataByAuthModule(jsonObject);
        if (StringUtils.isBlank(id))// 是否有权限
        {
            //无权限
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);  
        }
        else
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
        }
        return resultJsonObject;
    }
    
    /**
     * 助手进入公司文件查询是否有权限
     * 
     * @param map
     * @param info
     * @return
     * @Description:
     */
    private JSONObject commonCompany(Map<String, Object> map, InfoVo info)
    {
        LOG.debug("进入助手公司文件查询是否有权限");
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
        String belongTable = DAOUtil.getTableName("catalog_belong", info.getCompanyId());
        String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
        StringBuilder queryBuilder =
            new StringBuilder("select parent_id from ").append(catalogTable).append(" where id = ").append(map.get("id")).append(" and status =").append(Constant.CURRENCY_ZERO);
        Object object = DAOUtil.executeQuery4Object(queryBuilder.toString()); // 查询上级
        JSONObject resultJsonObject = new JSONObject();
        if (null == object)
        {
            resultJsonObject.put("readAuth", Constant.CURRENCY_TWO);
            return resultJsonObject;
        }
        String str = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.parseLong(map.get("id").toString()), 0, "catalog");
        if (StringUtils.isBlank(str))
        {
          //无权限
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
            return resultJsonObject;
        }
        String id = str.substring(0, str.indexOf(',')); // 获取顶级文件夹
        queryBuilder.setLength(0);
        queryBuilder.append(" select * from ").append(belongTable);
        queryBuilder.append(" where file_id =  ").append(id);
        JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
        if (null == jsonObject)
        { // 查询文件属性 是公开还是私有
             //无权限
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
            return resultJsonObject;
        }
        if (jsonObject.getInteger("type") == Constant.CURRENCY_ZERO)
        { // 公开都有查看权限
            resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
            return resultJsonObject;
        }
        else
        {
            StringBuilder builder = new StringBuilder();
            builder.append("select count(*) from ").append(manageTable).append(" where file_id = ").append(object).append(" and employee_id = ").append(info.getEmployeeId());
            int sum = DAOUtil.executeCount(builder.toString());
            if (sum <= 0)
            { // 查询上级是否是管理员
                StringBuilder buf =
                    new StringBuilder("select * from ").append(settingTable).append(" where file_id = ").append(id).append(" and  employee_id = ").append(info.getEmployeeId());
                JSONObject json = DAOUtil.executeQuery4FirstJSON(buf.toString());
                if (null == json)
                { // 查询是否是成员
                    // 无权限
                    resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
                }
                else
                {
                    resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
                }
            }
            else
            {
                resultJsonObject.put("readAuth", Constant.CURRENCY_ONE);
            }
            return resultJsonObject;
        }
        
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
            newModel.add(object.getLong("id"));
            newModel.add(Constant.CURRENCY_TWO);
            batchValue.add(newModel);
        }
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        StringBuilder updateBuilder =
            new StringBuilder("update ").append(catalogTable).append(" set  status = ").append(Constant.CURRENCY_ONE).append(",model_id = ?  where model_id = ?  and  type = ? ");
        int sum = DAOUtil.executeUpdate(updateBuilder.toString(), batchValue, 100000); // 批量修改所属那条数据的ID
        
        StringBuilder editBuilder =
            new StringBuilder("update ").append(catalogTable).append(" set  status = ").append(Constant.CURRENCY_ZERO).append(" , model_id = ?  where url = ?");
        int count = DAOUtil.executeUpdate(editBuilder.toString(), batchValues, 100000); // 批量修改所属那条数据的ID
        return true;
    }
    
    /**
     * 查看应用文件根目录
     * 
     */
    @Override
    public JSONObject queryAppFileList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        String id = applicationAppService.getApplicationsByUser(map);
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        if ("".equals(id)) // 获取能看得到的应用
        {
            result = BusinessDAOUtil.getTableDataListAndPageInfo("", pageSize, pageNum);
            return result;
        }
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select c.*,e.employee_name from ")
            .append(catalogTable)
            .append(" c left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id   where c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id is null  and c.model_id in (")
            .append(id)
            .append(") and c.type =  0   ")
            .append(" order by c.id asc");
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 查看应用模块列表
     * 
     */
    @Override
    public JSONObject queryModuleFileList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        String applicationId = moduleAppService.getModulesByApplication(map); // 获取应用下看到的模块文件夹
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        StringBuilder queryBuilder = new StringBuilder();
        if ("".equals(applicationId))
        {
            result = BusinessDAOUtil.getTableDataListAndPageInfo("", pageSize, pageNum);
            return result;
        }
        queryBuilder.append("select c.*,e.employee_name from  ")
            .append(catalogTable)
            .append(" c  left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where  c.table_id =")
            .append(map.get("style"))
            .append(" and parent_id = ")
            .append(map.get("applicationId"))
            .append("  and c.model_id in (")
            .append(applicationId)
            .append(")  and type =")
            .append(Constant.CURRENCY_ONE);
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 模块数据文件列表
     * 
     */
    @Override
    public JSONObject queryModulePartFileList(Map<String, Object> map)
    {
        JSONObject result = new JSONObject();
        InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String employeeTable = DAOUtil.getTableName("employee", info.getCompanyId());
        JSONObject object = new JSONObject();
        object.put("token", map.get("token").toString());
        object.put("module_id", map.get("applicationId").toString());
        String id = moduleDataAuthAppService.getModuleDataByAuthModule(object); // 获取模块下能看到的文件
        int pageSize = Integer.parseInt(map.get("pageSize").toString());
        int pageNum = Integer.parseInt(map.get("pageNum").toString());
        StringBuilder queryBuilder = new StringBuilder();
        if ("".equals(id))
        {
            result = BusinessDAOUtil.getTableDataListAndPageInfo("", pageSize, pageNum);
            return result;
        }
        queryBuilder.append("select c.*,e.employee_name,e.picture from  ")
            .append(catalogTable)
            .append(" c  left join ")
            .append(employeeTable)
            .append(" e on c.create_by = e.id  where   c.status = ")
            .append(Constant.CURRENCY_ZERO)
            .append(" and c.table_id =")
            .append(map.get("style"))
            .append("  and c.parent_id =")
            .append(map.get("applicationId"))
            .append(" and  model_id in (")
            .append(id)
            .append(") and c.type = ")
            .append(Constant.CURRENCY_TWO)
            .append(" order by c.id asc");
        
        result = BusinessDAOUtil.getTableDataListAndPageInfo(queryBuilder.toString(), pageSize, pageNum);
        return result;
    }
    
    /**
     * 生成公共链接
     */
    @Override
    public JSONObject queryFileLibraryUrl(Map<String, Object> map)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            StringBuilder buf = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            StringBuilder builder = new StringBuilder();
            builder.append("?id=").append(map.get("id")).append("&company_id=").append(info.getCompanyId()).append("&type=").append(map.get("type"));
            // 加密
            String encryUrl = Base64.getEncoder().encodeToString(builder.toString().getBytes());
            // 拼接生成链接
            buf.append(encryUrl);
            jsonObject.put("url", buf);
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return jsonObject;
    }
    
    /**
     * 保存公开链接添加
     */
    @Override
    public ServiceResult<String> openUrlSava(Map<String, Object> map)
    {
        StringBuilder buf = new StringBuilder();
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String openTable = DAOUtil.getTableName("open_file_url", info.getCompanyId());
            
            // 公开链接类型 1 地址 2 邮件 3 二维码
            if (Integer.parseInt(map.get("type").toString()) == 1)
            {
                if (null == map.get("visit_pwd") || "".equals(map.get("visit_pwd")))
                {
                    serviceResult.setCodeMsg(resultCode.get("postprocess.file.open.pwd.error"), resultCode.getMsgZh("postprocess.file.open.pwd.error"));
                    return serviceResult;
                }
            }
            else if (Integer.parseInt(map.get("type").toString()) == 2)
            {
                boolean flag = commonFile(map, info);
                if (!flag)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            buf.append("select count(*) from ").append(openTable).append(" where file_id = ").append(map.get("id"));
            int count = DAOUtil.executeCount(buf.toString());
            if (count <= 0)
            { // 是否存在公开链接 不存即添加
                StringBuilder insertSql = new StringBuilder();
                insertSql.append("insert into ")
                    .append(openTable)
                    .append("(file_id,visit_pwd,end_time,share_by,share_time) values (")
                    .append(map.get("id"))
                    .append(",'")
                    .append(map.get("visit_pwd"))
                    .append("',")
                    .append(map.get("end_time").equals("") ? null : map.get("end_time"))
                    .append(",")
                    .append(info.getEmployeeId())
                    .append(",")
                    .append(System.currentTimeMillis())
                    .append(")");
                int sum = DAOUtil.executeUpdate(insertSql.toString());
                if (sum <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
            else
            { // 存在即修改
                StringBuilder editSql = new StringBuilder();
                editSql.append("update ")
                    .append(openTable)
                    .append(" set end_time = ")
                    .append(map.get("end_time").equals("") ? null : map.get("end_time"))
                    .append(",share_by=")
                    .append(info.getEmployeeId())
                    .append(",share_time=")
                    .append(System.currentTimeMillis());
                if (!"".equals(map.get("visit_pwd")) && null != map.get("visit_pwd"))
                {
                    editSql.append(",visit_pwd = '").append(map.get("visit_pwd")).append("'");
                }
                editSql.append(" where file_id = ").append(map.get("id"));
                int num = DAOUtil.executeUpdate(editSql.toString());
                if (num <= 0)
                {
                    serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                    return serviceResult;
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 公开链接 邮件公开
     * 
     * @param map
     * @param info
     * @return
     * @Description:
     */
    private boolean commonFile(Map<String, Object> map, InfoVo info)
    {
        StringBuilder buf = new StringBuilder();
        try
        {
            String openTable = DAOUtil.getTableName("open_file_email", info.getCompanyId());
            buf.append("select count(*) from ")
                .append(openTable)
                .append(" where email = '")
                .append(map.get("email"))
                .append("' and file_id =")
                .append(map.get("id"))
                .append(" and create_by = ")
                .append(info.getEmployeeId());
            int count = DAOUtil.executeCount(buf.toString());
            // 邮箱是否发送过
            if (count <= 0)
            {
                StringBuilder insertSql = new StringBuilder();
                insertSql.append("insert into ")
                    .append(openTable)
                    .append("(file_id,email,create_by,create_time) values (")
                    .append(map.get("id"))
                    .append(",'")
                    .append(map.get("email"))
                    .append("',")
                    .append(info.getEmployeeId())
                    .append(",'")
                    .append(System.currentTimeMillis())
                    .append("')");
                int sum = DAOUtil.executeUpdate(insertSql.toString());
                if (sum <= 0)
                {
                    return false;
                }
            }
            else
            {
                StringBuilder editSql = new StringBuilder();
                editSql.append("update ").append(openTable).append(" set create_time = ").append(System.currentTimeMillis());
                editSql.append(" where file_id = ").append(map.get("id"));
                int num = DAOUtil.executeUpdate(editSql.toString());
                if (num <= 0)
                {
                    return false;
                }
            }
            
            mailOperationService.sendShareMail(map.get("token").toString(), map.get("email").toString(), map.get("content").toString(), "文件库分享");
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return false;
        }
        // 发送邮件操作待做
        return true;
    }
    
    /**
     * 获取公开链接分享历史列表
     */
    @Override
    public List<JSONObject> queryOpenUrlEmail(Map<String, Object> map)
    {
        StringBuilder queryBuf = new StringBuilder();
        List<JSONObject> list = new ArrayList<>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token").toString());
            String openTable = DAOUtil.getTableName("open_file_email", info.getCompanyId());
            queryBuf.append("select * from ").append(openTable).append(" where file_id = ").append(map.get("id")).append(" and create_by = ").append(info.getEmployeeId());
            list = DAOUtil.executeQuery4JSON(queryBuf.toString());
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return list;
    }
    
    /**
     * 获取公开链接文件详情
     */
    @Override
    public JSONObject queryFileUrlDetail(Map<String, String> map)
    {
        JSONObject json = new JSONObject();
        try
        {
            String str = ParseTxtFromFile.lBytesToString(Base64.getDecoder().decode(map.get("sign")), 0, Base64.getDecoder().decode(map.get("sign")).length, null);
            String companyId = str.substring(str.indexOf("company_id=") + 11, str.indexOf("&type"));
            String id = str.substring(str.indexOf("?id=") + 4, str.indexOf("&company_id"));
            String type = str.substring(str.indexOf("&type=") + 6, str.length());
            
            StringBuilder querySql = new StringBuilder();
            querySql.append("select * from open_file_url_").append(companyId).append(" where file_id = ").append(id);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            // 验证是否存在公开
            if (null == jsonObject)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.open.download.time.error");
            }
            // 判断有效时长
            StringBuilder queryBuf = new StringBuilder();
            String catalogTable = DAOUtil.getTableName("catalog", companyId);
            queryBuf.append("select siffix,size,name from ").append(catalogTable).append(" where id = ").append(id);
            json = DAOUtil.executeQuery4FirstJSON(queryBuf.toString());
            if (null != jsonObject.getLong("end_time"))
            {
                if (System.currentTimeMillis() > jsonObject.getLong("end_time"))
                {
                    json.put("end_time", "分享链接失效");
                }
            }
            // 是否需要填写密码
            if ("0".equals(type))
            {
                json.put("visit_pwd", "需要填写分享密码");
            }
            
            StringBuilder query = new StringBuilder();
            String employeeTable = DAOUtil.getTableName("employee", companyId);
            query.append("select employee_name,picture from ").append(employeeTable).append(" where id = ").append(jsonObject.getLong("share_by"));
            JSONObject jsonResult = DAOUtil.executeQuery4FirstJSON(query.toString());
            json.put("share_by", jsonResult);
            json.put("share_time", jsonObject.get("share_time"));
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return json;
    }
    
    /**
     * 公开链接密码验证
     */
    @Override
    public ServiceResult<String> openUrlVailPwd(Map<String, String> map)
    {
        ServiceResult<String> serviceResult = new ServiceResult<>();
        try
        {
            String str = ParseTxtFromFile.lBytesToString(Base64.getDecoder().decode(map.get("sign")), 0, Base64.getDecoder().decode(map.get("sign")).length, null);
            String companyId = str.substring(str.indexOf("company_id=") + 11, str.indexOf("&type"));
            String id = str.substring(str.indexOf("?id=") + 4, str.indexOf("&company_id"));
            StringBuilder querySql = new StringBuilder().append("select * from open_file_url_").append(companyId).append(" where file_id = ").append(id);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            if (!jsonObject.getString("visit_pwd").equals(map.get("visit_pwd")))
            {
                serviceResult.setCodeMsg(resultCode.get("common.fail"), resultCode.getMsgZh("common.fail"));
                return serviceResult;
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        serviceResult.setCodeMsg(resultCode.get("common.sucess"), resultCode.getMsgZh("common.sucess"));
        return serviceResult;
    }
    
    /**
     * 新增加员工默认添加为成员
     */
    @Override
    public boolean savaFileMember(String token, Long employeeId)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
        String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
        StringBuilder queryBuilder = new StringBuilder();
        queryBuilder.append("select * from ");
        queryBuilder.append(catalogTable);
        queryBuilder.append(" where table_id =   ");
        queryBuilder.append(Constant.CURRENCY_ONE);
        queryBuilder.append(" and status =   ");
        queryBuilder.append(Constant.CURRENCY_ZERO);
        queryBuilder.append("  and parent_id is null  ");
        List<JSONObject> jsonList = DAOUtil.executeQuery4JSON(queryBuilder.toString());
        List<List<Object>> batchValues = new ArrayList<>();
        if (!jsonList.isEmpty())
        {
            for (int i = 0; i < jsonList.size(); i++)
            {
                List<Object> model = new ArrayList<>();
                JSONObject jsonObject = jsonList.get(i);
                model.add(jsonObject.getLong("id"));
                model.add(employeeId);
                batchValues.add(model);
            }
        }
        if (!batchValues.isEmpty())
        {
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append(" insert into ");
            insertBuilder.append(settingTable);
            insertBuilder.append(" (file_id,employee_id)");
            insertBuilder.append(" values(?,?)");
            int count = DAOUtil.executeUpdate(insertBuilder.toString(), batchValues, 10000);
        }
        return true;
    }
    
    /**
     * 验证上级文件夹及顶级权限
     * 
     * @param token
     * @param id
     * @return
     * @Description:
     */
    @Override
    public boolean vailFileLibararyAuth(String token, Long id)
    {
        
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            StringBuilder queryBuilder =
                new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(id).append(" and status =").append(Constant.CURRENCY_ZERO);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString()); // 查询上级parent_id
            if (null == jsonObject)
            {
                return false;
            }
            if (jsonObject.getInteger("table_id") == 1)
            {
                StringBuilder queryCountBuilder = new StringBuilder();
                queryCountBuilder.append("select count(1) from ");
                queryCountBuilder.append(manageTable);
                queryCountBuilder.append(" where file_id = ");
                queryCountBuilder.append(jsonObject.getLong("parent_id"));
                queryCountBuilder.append(" and employee_id = ");
                queryCountBuilder.append(info.getEmployeeId());
                // 查询是否有权限操作
                int number = DAOUtil.executeCount(queryCountBuilder.toString());
                if (number <= 0)
                {
                    String str = BusinessDAOUtil.getFileDepments(info.getCompanyId(), id, 0, "catalog");
                    if (StringUtils.isBlank(str))
                    {
                        return false;
                    }
                    String dataId = str.substring(0, str.indexOf(',')); // 获取顶级文件夹
                    StringBuilder buf = new StringBuilder();
                    buf.append("select count(*) from ");
                    buf.append(settingTable);
                    buf.append(" where file_id = ");
                    buf.append(dataId);
                    buf.append(" and  employee_id = ");
                    buf.append(info.getEmployeeId());
                    buf.append(" and download = ");
                    buf.append(Constant.CURRENCY_ONE);
                    int sum = DAOUtil.executeCount(buf.toString());
                    if (sum <= 0)
                    { // 查询是否是成员
                        return false;
                    }
                    else
                    {
                        return true;
                    }
                }
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
     * 验证本级文件夹及顶级权限
     * 
     * @param token
     * @param id
     * @param type 类型 0 上传 1 下载
     * @return
     * @Description:
     */
    @Override
    public boolean vailFileAuth(String token, Long id)
    {
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            StringBuilder queryBuilder =
                new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(id).append(" and status =").append(Constant.CURRENCY_ZERO);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString()); // 查询
            if (jsonObject.getInteger("table_id") == 1)// 公司文件
            {
                StringBuilder queryCountBuilder = new StringBuilder();
                queryCountBuilder.append("select count(1) from ");
                queryCountBuilder.append(manageTable);
                queryCountBuilder.append(" where file_id = ");
                queryCountBuilder.append(id);
                queryCountBuilder.append(" and employee_id = ");
                queryCountBuilder.append(info.getEmployeeId());
                // 查询是否有权限操作
                int number = DAOUtil.executeCount(queryCountBuilder.toString());
                if (number <= 0)
                {
                    String str = BusinessDAOUtil.getFileDepments(info.getCompanyId(), id, 0, "catalog");
                    if (StringUtils.isBlank(str))
                    {
                        return false;
                    }
                    String dataId;
                    if (str.indexOf(',') > -1)
                    {
                        dataId = str.substring(0, str.indexOf(',')); // 获取顶级文件夹
                    }
                    else
                    {
                        dataId = str;
                    }
                    StringBuilder buf = new StringBuilder();
                    buf.append("select count(*) from ");
                    buf.append(settingTable);
                    buf.append(" where file_id = ");
                    buf.append(dataId);
                    buf.append(" and  employee_id = ");
                    buf.append(info.getEmployeeId());
                    buf.append(" and download = ");
                    buf.append(Constant.CURRENCY_ONE);
                    
                    int sum = DAOUtil.executeCount(buf.toString());
                    if (sum <= 0)
                    { // 查询是否是成员
                        return false;
                    }
                }
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
     * 验证邮件选文件库文件权限
     * 
     * @param map
     * @return
     * @Description:
     */
    @Override
    public JSONObject queryEmailFileAuth(Map<String, String> map)
    {
        JSONObject json = new JSONObject();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(map.get("token"));
            String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
            String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
            String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
            StringBuilder queryBuilder =
                new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(map.get("id")).append(" and status =").append(Constant.CURRENCY_ZERO);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString()); // 查询上级parent_id
            if (null == jsonObject)
            {
                json.put("mailAuth", Constant.CURRENCY_ZERO);
                return json;
            }
            if (jsonObject.getInteger("table_id") == 1)
            {
                StringBuilder queryCountBuilder = new StringBuilder();
                queryCountBuilder.append("select count(1) from ");
                queryCountBuilder.append(manageTable);
                queryCountBuilder.append(" where file_id = ");
                queryCountBuilder.append(jsonObject.getLong("parent_id"));
                queryCountBuilder.append(" and employee_id = ");
                queryCountBuilder.append(info.getEmployeeId());
                // 查询是否有权限操作
                int number = DAOUtil.executeCount(queryCountBuilder.toString());
                if (number <= 0)
                {
                    String str = BusinessDAOUtil.getFileDepments(info.getCompanyId(), Long.valueOf(map.get("id")), 0, "catalog");
                    if (StringUtils.isBlank(str))
                    {
                        json.put("mailAuth", Constant.CURRENCY_ZERO);
                        return json;
                    }
                    String dataId = str.substring(0, str.indexOf(',')); // 获取顶级文件夹
                    StringBuilder buf = new StringBuilder();
                    buf.append("select count(*) from ");
                    buf.append(settingTable);
                    buf.append(" where file_id = ");
                    buf.append(dataId);
                    buf.append(" and  employee_id = ");
                    buf.append(info.getEmployeeId());
                    buf.append(" and download = ");
                    buf.append(Constant.CURRENCY_ONE);
                    int sum = DAOUtil.executeCount(buf.toString());
                    if (sum <= 0)
                    { // 查询是否是成员
                        json.put("mailAuth", Constant.CURRENCY_ZERO);
                        return json;
                    }
                    
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            json.put("mailAuth", Constant.CURRENCY_ZERO);
            return json;
        }
        json.put("mailAuth", Constant.CURRENCY_ONE);
        return json;
    }
    
    /**
     * 验证上传是否拥有权限
     * 
     * @param token
     * @param id
     * @return
     * @Description:
     */
    @Override
    public boolean vailFileUploadAuth(String token, Long id, String type)
    {
        try
        {
            if (Integer.parseInt(type) == Constant.CURRENCY_ONE)
            {
                InfoVo info = TokenMgr.obtainInfo(token);
                String catalogTable = DAOUtil.getTableName("catalog", info.getCompanyId());
                String manageTable = DAOUtil.getTableName("catalog_manage", info.getCompanyId());
                String settingTable = DAOUtil.getTableName("catalog_setting", info.getCompanyId());
                StringBuilder queryBuilder =
                    new StringBuilder("select * from ").append(catalogTable).append(" where id = ").append(id).append(" and status =").append(Constant.CURRENCY_ZERO);
                JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString()); // 查询
                if (jsonObject.getInteger("table_id") == 1)// 公司文件
                {
                    StringBuilder queryCountBuilder = new StringBuilder();
                    queryCountBuilder.append("select count(1) from ");
                    queryCountBuilder.append(manageTable);
                    queryCountBuilder.append(" where file_id = ");
                    queryCountBuilder.append(id);
                    queryCountBuilder.append(" and employee_id = ");
                    queryCountBuilder.append(info.getEmployeeId());
                    // 查询是否有权限操作
                    int number = DAOUtil.executeCount(queryCountBuilder.toString());
                    if (number <= 0)
                    {
                        String str = BusinessDAOUtil.getFileDepments(info.getCompanyId(), id, 0, "catalog");
                        if (StringUtils.isBlank(str))
                        {
                            return false;
                        }
                        String dataId;
                        if (str.indexOf(',') > -1)
                        {
                            dataId = str.substring(0, str.indexOf(',')); // 获取顶级文件夹
                        }
                        else
                        {
                            dataId = str;
                        }
                        StringBuilder buf = new StringBuilder();
                        buf.append("select count(*) from ");
                        buf.append(settingTable);
                        buf.append(" where file_id = ");
                        buf.append(dataId);
                        buf.append(" and  employee_id = ");
                        buf.append(info.getEmployeeId());
                        buf.append(" and upload= ");
                        buf.append(Constant.CURRENCY_ONE);
                        
                        int sum = DAOUtil.executeCount(buf.toString());
                        if (sum <= 0)
                        { // 查询是否是成员
                            return false;
                        }
                    }
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return false;
        }
        return true;
    }
    
}
