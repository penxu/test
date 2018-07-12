package com.teamface.im.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.im.constant.ImConstant;

/**
 * 
 * @Title:
 * @Description:人员组件获取发送者ID的工具类
 * @Author:dsl
 * @Since:2018年1月23日
 * @Version:1.1.0
 */
public class DynamicParameterUtil
{
    /**
     * 
     * @param field
     * @param beanName
     * @param companyId
     * @param id
     * @return
     * @Description:通过字段名查询signId
     */
    public static Long getSignIdByField(String field, String beanName, Long companyId, Long id)
    {
        StringBuilder querySignIdSql = new StringBuilder().append("select a.id from acountinfo a ,")
            .append(beanName)
            .append(" bean where a.company_id = ")
            .append(companyId)
            .append(" and a.employee_id = bean.")
            .append(field)
            .append(" and bean.id = ")
            .append(id);
        JSONObject result = DAOUtil.executeQuery4FirstJSON(querySignIdSql.toString());
        if (null != result)
        {
            return result.getLongValue("id");
        }
        return null;
    }
    
    /**
     * 
     * @param field
     * @param beanName
     * @param companyId
     * @param id
     * @return
     * @Description:通过字段名查询人员的上级
     */
    public static Long getSuperiorSignIdByField(String field, String beanName, Long companyId, Long id)
    {
        String beanField = field.substring(0, field.lastIndexOf("_"));
        StringBuilder querySignIdSql = new StringBuilder().append("select a.id,a.department_id,a.employee_id from department_center_")
            .append(companyId)
            .append(" dcn join (select * from ")
            .append(beanName)
            .append(" where id=")
            .append(id)
            .append(") bean on dcn.employee_id = bean.")
            .append(beanField)
            .append(" join (select * from department_center_")
            .append(companyId)
            .append(" where status=0 and leader=1 and is_main=1) dcnt on dcn.department_id = dcnt.department_id")
            .append(" join acountinfo a on a.employee_id=dcnt.employee_id")
            .append(" where a.company_id = ")
            .append(companyId)
            .append(" and dcn.status = 0 ");
        JSONObject result = DAOUtil.executeQuery4FirstJSON(querySignIdSql.toString());
        if (null != result)
        {
            // 查询负责人员工ID
            StringBuilder queryPrincipalSqlSB = new StringBuilder("select ").append(beanField).append(" from ");
            queryPrincipalSqlSB.append(beanName).append(" where id = ").append(id);
            JSONObject principalObj = DAOUtil.executeQuery4FirstJSON(queryPrincipalSqlSB.toString());
            // 判断主部门负责人是否是本人 如果不是返回员工signId 如果是查询部门上级负责人的signId
            if (result.getLong("employee_id") != principalObj.getLong(beanField))
            {
                return result.getLong("id");
            }
            // 根据本级部门id查询上级部门负责人的signId
            querySignIdSql.setLength(0);
            querySignIdSql.append(" select * from  department_center_");
            querySignIdSql.append(companyId);
            querySignIdSql.append(" c left join acountinfo a on c.employee_id = a.employee_id  where  c.department_id = ( select parent_id from department_");
            querySignIdSql.append(companyId);
            querySignIdSql.append(" where id  =  ");
            querySignIdSql.append(result.getLong("department_id"));
            querySignIdSql.append(" and status = 0 ) and c.leader=1  and  c.status = 0  and a.company_id =  ");
            querySignIdSql.append(companyId);
            JSONObject superPrincipal = DAOUtil.executeQuery4FirstJSON(querySignIdSql.toString());
            // 如果有上级部门的负责人则返回上级部门负责人
            if (!Objects.isNull(superPrincipal))
            {
                return superPrincipal.getLong("id");
            }
            
        }
        return null;
    }
    
    /**
     * 
     * @param field
     * @param beanName
     * @param companyId
     * @param id
     * @return
     * @Description:获取共享的signId
     */
    public static List<String> getShareSignIdByField(String field, String beanName, Long companyId, Long id)
    {
        
        StringBuilder queryShareInfoSql = new StringBuilder().append("select * from module_data_share_setting_").append(companyId).append(" where module_id = ").append(id);
        List<JSONObject> result = DAOUtil.executeQuery4JSON(queryShareInfoSql.toString());
        String allotEmployee;
        List<String> alertPeoples = new ArrayList<String>();
        for (int i = 0; i < result.size(); i++)
        {
            allotEmployee = result.get(i).getString("allot_employee");
            JSONArray jsonArray = JSONObject.parseArray(allotEmployee);
            for (Object object : jsonArray)
            {
                JSONObject peopleJson = (JSONObject)object;
                Short selectedType = peopleJson.getShort("type");
                if (selectedType == ImConstant.STRUCTURE_TYPE_MEMBER)
                {
                    alertPeoples.add(peopleJson.getString("sign_id"));
                }
                // 根据部门查询用户的signId
                if (selectedType == ImConstant.STRUCTURE_TYPE_DEPARTMENT)
                {
                    long depId = peopleJson.getLongValue("id");
                    getSignIdByDept(alertPeoples, companyId, depId);
                }
                // 根据角色查询用户的signId
                if (selectedType == ImConstant.STRUCTURE_TYPE_ROLE)
                {
                    long roleId = peopleJson.getLongValue("id");
                    getSignIdByRole(alertPeoples, companyId, roleId);
                }
                // 查询所有公司下面的所有人员
                if (selectedType == ImConstant.STRUCTURE_TYPE_COMPANY)
                {
                    getSignIdByCompany(alertPeoples, companyId);
                }
            }
        }
        return alertPeoples;
    }
    
    /**
     * 
     * @param alertPeoples
     * @param companyId
     * @Description:获取公司所有人员的signId
     */
    public static void getSignIdByCompany(List<String> alertPeoples, Long companyId)
    {
        StringBuilder queryCompanySqlSB =
            new StringBuilder().append("select a.id from acountinfo a,employee_").append(companyId).append(" e where a.company_id = ").append(companyId).append(
                " and a.employee_id = e.id and e.status = 0 and del_status = 0");
        List<Map<String, Object>> patchMems = DAOUtil.executeQuery(queryCompanySqlSB.toString());
        for (int k = 0; k < patchMems.size(); k++)
        {
            alertPeoples.add(String.valueOf(patchMems.get(k).get("id")));
        }
    }
    
    /**
     * 
     * @param alertPeoples
     * @param companyId
     * @param roleId
     * @Description:根据角色查询signid
     */
    public static void getSignIdByRole(List<String> alertPeoples, Long companyId, Long roleId)
    {
        StringBuilder queryRoleSqlSB = new StringBuilder().append("select a.id from role_")
            .append(companyId)
            .append(" r,employee_")
            .append(companyId)
            .append(" e,acountinfo a where r.id = ")
            .append(roleId)
            .append(" and e.role_id = r.id and e.id = a.employee_id and a.company_id = ")
            .append(companyId);
        List<Map<String, Object>> patchMems = DAOUtil.executeQuery(queryRoleSqlSB.toString());
        for (int j = 0; j < patchMems.size(); j++)
        {
            alertPeoples.add(String.valueOf(patchMems.get(j).get("id")));
        }
    }
    
    /**
     * 
     * @param alertPeoples
     * @param companyId
     * @param deptId
     * @Description:查询部门下面所有的人员的signId
     */
    public static void getSignIdByDept(List<String> alertPeoples, Long companyId, Long deptId)
    {
        // 获取当前部门的所有子部门
        String deptIds = BusinessDAOUtil.getDepments(companyId, deptId, "department");
        StringBuilder queryDeptSqlSB = new StringBuilder().append("select a.id from acountinfo a join (select dc.employee_id from department_center_")
            .append(companyId)
            .append(" dc where dc.department_id in (")
            .append(deptIds)
            .append(") and dc.status = 0) depIds on depIds.employee_id = a.employee_id")
            .append(" where a.company_id = ")
            .append(companyId);
        List<Map<String, Object>> patchMems = DAOUtil.executeQuery(queryDeptSqlSB.toString());
        for (int k = 0; k < patchMems.size(); k++)
        {
            alertPeoples.add(String.valueOf(patchMems.get(k).get("id")));
        }
    }
    
    /**
     * 
     * @param field
     * @param beanName
     * @param companyId
     * @param id
     * @return
     * @Description:查询转移负责人signId
     */
    public static Long getTransferSignIdByField(String beanName, Long companyId, Long id)
    {
        StringBuilder querySignIdSql = new StringBuilder().append("select a.id from acountinfo a ,")
            .append(beanName)
            .append(" bean where a.company_id = ")
            .append(companyId)
            .append(" and a.employee_id = bean.personnel_principal and bean.id = ")
            .append(id);
        JSONObject result = DAOUtil.executeQuery4FirstJSON(querySignIdSql.toString());
        if (null != result)
        {
            return result.getLongValue("id");
        }
        return null;
    }
}
