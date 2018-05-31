package com.teamface.filelibrary.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.NodeUtil;
import com.teamface.common.util.dao.NodeUtil.Node;

import io.jsonwebtoken.lang.Collections;

/**
 * @Description:文件库搜索
 * @author: dsl
 * @date: 2018年2月1日 上午10:24:10
 * @version: 1.0
 */
public class SearchLibFile
{
    private static Map<String, Node> nodeMap;
    
    /**
     * 
     * @param companyId 公司ID
     * @param id 公司文件类型
     * @param employeeId 员工ID
     * @param content 搜索文件内容
     * @return
     * @Description:搜索公司文件所有文件
     */
    public static List<JSONObject> searchCompanyFile(Long companyId, Long id, Long employeeId, String content)
    {
        List<JSONObject> result = new ArrayList<>();
        StringBuilder searchManagerBlurQuerySB = new StringBuilder();
        StringBuilder searchMemberBlurQuerySB = new StringBuilder();
        getLibId(companyId, "catalog");
        // 查询管理员权限的文件夹
        searchManagerBlurQuerySB.append("select c.id,1 as role_type,cb.type from catalog_").append(companyId).append(" c,catalog_manage_");
        searchManagerBlurQuerySB.append(companyId).append(" cm,catalog_belong_").append(companyId);
        searchManagerBlurQuerySB.append(" cb where c.id = cb.file_id and cm.file_id = c.id and cm.employee_id = ");
        searchManagerBlurQuerySB.append(employeeId).append(" and table_id = ").append(id);
        searchManagerBlurQuerySB.append(" and cm.sign_type = 0").append(" order by c.id");
        List<JSONObject> managerList = DAOUtil.executeQuery4JSON(searchManagerBlurQuerySB.toString());
        StringBuilder managerIdsSB = new StringBuilder();
        if (!Collections.isEmpty(managerList))
        {
            for (int i = 0; i < managerList.size(); i++)
            {
                // 查询所有子目录或文件
                Long publicId = managerList.get(i).getLongValue("id");
                String publicChildIds = getAllSubordinate(publicId);
                managerIdsSB.append(managerIdsSB.length() == 0 ? "" : ",");
                managerIdsSB.append(publicChildIds);
            }
            if (!StringUtil.isEmpty(managerIdsSB))
            {
                StringBuilder queryAllCatalog = new StringBuilder().append("select c.*,e.employee_name,1 as role_type,1 as upload,1 as download,1 as preview from catalog_");
                queryAllCatalog.append(companyId).append(" c join (select * from employee_").append(companyId).append(") e on c.create_by = e.id where c.id in (");
                queryAllCatalog.append(managerIdsSB).append(") and c.name like '%").append(content).append("%' order by c.id");
                List<JSONObject> publicChildJsons = DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
                for (JSONObject resObj : publicChildJsons)
                {
                    for (int j = 0; j < managerList.size(); j++)
                    {
                        if (managerList.get(j).getLongValue("id") == resObj.getLongValue("id"))
                        {
                            resObj.put("type", managerList.get(j).get("type"));
                        }
                    }
                }
                result.addAll(publicChildJsons);
            }
        }
        
        // 查询成员的文件夹
        searchMemberBlurQuerySB.append("select c.*,cs.employee_id,cs.upload,cs.download,cs.preview,2 as role_type,cb.type from catalog_").append(companyId);
        searchMemberBlurQuerySB.append(" c join (select * from catalog_setting_").append(companyId).append("  where employee_id = ").append(employeeId);
        searchMemberBlurQuerySB.append(" ) cs on c.id = cs.file_id join catalog_belong_").append(companyId);
        searchMemberBlurQuerySB.append(" cb on c.id = cb.file_id where c.table_id =").append(id).append(" order by c.id");
        List<JSONObject> memberList = DAOUtil.executeQuery4JSON(searchMemberBlurQuerySB.toString());
        StringBuilder memberListIdsSB = new StringBuilder();
        if (!Collections.isEmpty(memberList))
        {
            for (int i = 0; i < memberList.size(); i++)
            {
                // 查询所有子目录或文件
                Long publicId = memberList.get(i).getLongValue("id");
                String publicChildIds = getAllSubordinate(publicId);
                memberListIdsSB.append(memberListIdsSB.length() == 0 ? "" : ",");
                memberListIdsSB.append(publicChildIds);
            }
            if (!StringUtil.isEmpty(memberListIdsSB))
            {
                StringBuilder queryAllCatalog = new StringBuilder().append("select c.*,2 as role_type,e.employee_name").append(" from catalog_").append(companyId);
                queryAllCatalog.append(" c  join employee_").append(companyId).append(" e on e.id = c.create_by where c.id in (");
                queryAllCatalog.append(memberListIdsSB).append(") and c.name like '%").append(content).append("%' order by c.id ");
                List<JSONObject> publicChildJsons = DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
                for (int i = 0; i < memberList.size(); i++)
                {
                    JSONObject previllage = new JSONObject();
                    previllage.put("upload", memberList.get(i).get("upload"));
                    previllage.put("download", memberList.get(i).get("download"));
                    previllage.put("preview", memberList.get(i).get("preview"));
                    previllage.put("type", memberList.get(i).get("type"));
                    for (int k = 0; k < publicChildJsons.size(); k++)
                    {
                        if (publicChildJsons.get(k).getLongValue("id") == memberList.get(i).getLongValue("id"))
                        {
                            publicChildJsons.get(k).putAll(previllage);
                        }
                    }
                }
                result.addAll(publicChildJsons);
            }
        }
        return result;
    }
    
    /**
     * 
     * @param companyId 公司ID
     * @param id 搜索文件的类型
     * @param employeeId 员工ID
     * @param content 查询文件的内容
     * @param fileId 文件夹的ID
     * @return
     * @Description:搜索公司文件子文件的内容
     */
    public static List<JSONObject> searchCompanyFile(Long companyId, Long id, Long employeeId, String content, Long fileId)
    {
        List<JSONObject> result = new ArrayList<>();
        getLibId(companyId, "catalog");
        
        StringBuilder memberListIdsSB = new StringBuilder();
        if (!Objects.isNull(fileId))
        {
            String publicChildIds = getAllSubordinate(fileId);
            memberListIdsSB.append(memberListIdsSB.length() == 0 ? "" : ",");
            memberListIdsSB.append(publicChildIds);
            StringBuilder queryAllCatalog = new StringBuilder().append("select c.*,e.employee_name,1 as role_type,1 as upload,1 as download,1 as preview from catalog_");
            queryAllCatalog.append(companyId).append(" c join (select * from employee_").append(companyId).append(") e on c.create_by = e.id where c.id in (");
            queryAllCatalog.append(memberListIdsSB).append(") and c.name like '%").append(content).append("%' order by c.id");
            List<JSONObject> publicChildJsons = DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
            
            result.addAll(publicChildJsons);
        }
        
        return result;
    }
    
    /**
     * 
     * @param companyId 公司ID
     * @param id 搜索文件的类型
     * @param employeeId 员工ID
     * @param content 查询文件的内容
     * @return
     * @Description:搜索我共享的文件
     */
    public static List<JSONObject> searchShareToFile(Long companyId, Long id, Long employeeId, String content)
    {
        List<JSONObject> result = new ArrayList<>();
        getLibId(companyId, "catalog");
        // 查询满足条件的我共享的数据
        StringBuilder searchBlurQuerySB = new StringBuilder();
        searchBlurQuerySB.append("select c.*,cc.cover_by,cc.cover_status,e.employee_name from catalog_cover_").append(companyId);
        searchBlurQuerySB.append(" cc,catalog_").append(companyId).append(" c join employee_").append(companyId);
        searchBlurQuerySB.append(" e on c.create_by = e.id where cc.file_id = c.ID ");
        searchBlurQuerySB.append(" and cc.cover_by = ").append(employeeId).append(" and cc.cover_status = 0");
        List<JSONObject> catalogList = DAOUtil.executeQuery4JSON(searchBlurQuerySB.toString());
        StringBuilder catalogIdsSB = new StringBuilder();
        if (!Collections.isEmpty(catalogList))
        {
            // 获取我共享文件所有子集的文件
            for (int i = 0; i < catalogList.size(); i++)
            {
                Long publicId = catalogList.get(i).getLongValue("id");
                String publicChildIds = getAllSubordinate(publicId);
                catalogIdsSB.append(catalogIdsSB.length() == 0 ? "" : ",");
                catalogIdsSB.append(publicChildIds);
            }
            if (!StringUtil.isEmpty(catalogIdsSB))
            {
                StringBuilder queryAllCatalog = new StringBuilder().append("select c.*,e.employee_name from catalog_");
                queryAllCatalog.append(companyId).append(" c join (select * from employee_").append(companyId).append(") e on c.create_by = e.id where c.id in (");
                queryAllCatalog.append(catalogIdsSB).append(") and c.name like '%").append(content).append("%' order by c.id");
                List<JSONObject> publicChildJsons = DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
                result.addAll(publicChildJsons);
            }
        }
        
        return result;
    }
    
    /**
     * 
     * @param companyId 公司ID
     * @param id 搜索文件的类型
     * @param employeeId 员工ID
     * @param content 查询文件的内容
     * @return
     * @Description:搜索共享给我的文件
     */
    public static List<JSONObject> searchToShareFile(Long companyId, Long id, Long employeeId, String content)
    {
        List<JSONObject> result = new ArrayList<>();
        getLibId(companyId, "catalog");
        StringBuilder searchBlurQuerySB = new StringBuilder();
        searchBlurQuerySB.append("select c.*,cs.cover_id,cs.share_by,cs.share_status,e.employee_name from catalog_share_").append(companyId);
        searchBlurQuerySB.append(" cs,catalog_").append(companyId).append(" c join employee_").append(companyId);
        searchBlurQuerySB.append(" e on c.create_by = e.id where cs.file_id = c.ID ");
        searchBlurQuerySB.append(" and cs.share_by = ").append(employeeId).append(" and cs.share_status = 0");
        List<JSONObject> catalogList = DAOUtil.executeQuery4JSON(searchBlurQuerySB.toString());
        StringBuilder catalogIdsSB = new StringBuilder();
        if (!Collections.isEmpty(catalogList))
        {
            
            for (int i = 0; i < catalogList.size(); i++)
            {
                // 查询所有子目录或文件
                Long publicId = catalogList.get(i).getLongValue("id");
                String publicChildIds = getAllSubordinate(publicId);
                catalogIdsSB.append(catalogIdsSB.length() == 0 ? "" : ",");
                catalogIdsSB.append(publicChildIds);
            }
            if (!StringUtil.isEmpty(catalogIdsSB))
            {
                StringBuilder queryAllCatalog = new StringBuilder().append("select c.*,e.employee_name from catalog_");
                queryAllCatalog.append(companyId).append(" c join (select * from employee_").append(companyId).append(") e on c.create_by = e.id where c.id in (");
                queryAllCatalog.append(catalogIdsSB).append(") and c.name like '%").append(content).append("%' order by c.id");
                List<JSONObject> publicChildJsons = DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
                result.addAll(publicChildJsons);
            }
        }
        return result;
    }
    
    /**
     * 
     * @param companyId 公司ID
     * @param id 搜索文件的类型
     * @param employeeId 员工ID
     * @param content 查询文件的内容
     * @param fileId 文件夹的ID
     * @return
     * @Description:搜索文件的内容
     */
    public static List<JSONObject> searchShareFile(Long companyId, Long id, Long employeeId, String content, Long fileId)
    {
        List<JSONObject> result = new ArrayList<>();
        getLibId(companyId, "catalog");
        StringBuilder memberListIdsSB = new StringBuilder();
        if (!Objects.isNull(fileId))
        {
            String publicChildIds = getAllSubordinate(fileId);
            memberListIdsSB.append(memberListIdsSB.length() == 0 ? "" : ",");
            memberListIdsSB.append(publicChildIds);
            StringBuilder queryAllCatalog = new StringBuilder().append("select c.*,e.employee_name from catalog_");
            queryAllCatalog.append(companyId).append(" c join (select * from employee_").append(companyId).append(") e on c.create_by = e.id where c.id in (");
            queryAllCatalog.append(memberListIdsSB).append(") and c.name like '%").append(content).append("%' order by c.id");
            List<JSONObject> publicChildJsons = DAOUtil.executeQuery4JSON(queryAllCatalog.toString());
            result.addAll(publicChildJsons);
        }
        return result;
    }
    
    /**
     * 
     * @param companyId 公司ID
     * @param bean 表名
     * @Description: 获取该表下面所有层级的ID
     */
    private static void getLibId(long companyId, String bean)
    {
        StringBuilder sqlSB = new StringBuilder();
        String libTableName = DAOUtil.getTableName(bean, companyId);
        sqlSB.append("select id,parent_id from ").append(libTableName).append(" where table_id != 2 order by id");
        List<Node> nodes = DAOUtil.executeQuery4Node(sqlSB.toString());
        nodeMap = NodeUtil.setNodes(nodes);
    }
    
    /**
     * 
     * @param currentId 所属层级的ID
     * @return
     * @Description: 获取该层级下面所有的子集
     */
    private static String getAllSubordinate(long currentId)
    {
        StringBuilder libIDSB = new StringBuilder();
        NodeUtil.getNodeSubAndLocalNode(String.valueOf(currentId), nodeMap, libIDSB);
        return libIDSB.toString();
    }
}
