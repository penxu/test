package com.teamface.common.util.activiti;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;

/**
 * @Description:
 * @author: ZZH
 * @date: 2017年12月19日 下午2:53:39
 * @version: 1.0
 */

public class AssigneeServer
{
    
    final Logger log = Logger.getLogger(AssigneeServer.class);
    
    /**
     * 
     * @param companyId 公司编号
     * @param starter 发起人
     * @param level 部门级别
     * @param useUpper useUpper 若该审批人空缺，由其在通讯录中的上级部门负责人代审批
     * @return String
     * @Description:获取部门负责人
     */
    public String getDeparmentPrincipal(String companyId, String starter, Integer level, Boolean useUpper)
    {
        log.warn(String.format("parameters{args0:%s,args1:%s,args2:%s,args3:%s} start!", companyId, starter, level, useUpper));
        StringBuilder sqlSB = new StringBuilder();
        String tableName = DAOUtil.getTableName(Constant.TABLE_DEPARTMENT_CENTER, companyId);
        sqlSB.append("select department_id,leader from ").append(tableName).append(" where status=0 and employee_id=").append(starter);
        Map<String, Object> queryMap = DAOUtil.executeQuery4One(sqlSB.toString());
        Object id = queryMap.get("department_id");
        if (id != null)
        {
            int depId = Integer.parseInt(id.toString());
            String upDep = BusinessDAOUtil.getDepments(Long.parseLong(companyId), depId, 0);
            int leader = Integer.parseInt(queryMap.get("leader").toString());
            String[] dep = upDep.split(",");
            int size = dep.length;
            if (size > 0)
            {
                if (level > size)
                {
                    level = size;
                }
                if (leader == 1)
                {
                    level = level + 1;
                }
                if (level > size)
                {
                    return "";
                }
                String finalDepId = dep[size - level];
                String userId = getDeparmentPrincipal(companyId, finalDepId);
                // 若该审批人空缺，由其在通讯录中的上级部门负责人代审批
                if (useUpper)
                {
                    while (StringUtils.isEmpty(userId))
                    {
                        level = level + 1;
                        if (level > size)
                        {
                            return null;
                        }
                        finalDepId = dep[size - level];
                        userId = getDeparmentPrincipal(companyId, finalDepId);
                    }
                }
                log.warn("getDeparmentPrincipal deparmentId:" + finalDepId + ",employee:" + userId);
                return userId;
            }
        }
        log.warn("end!");
        return null;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param deparmentId 部门编号
     * @return String
     * @Description:获取部门负责人
     */
    public String getDeparmentPrincipal(String companyId, String deparmentId)
    {
        log.warn(String.format("parameters{args0:%s,args1:%s} start!", companyId, deparmentId));
        String tableName = DAOUtil.getTableName(Constant.TABLE_DEPARTMENT_CENTER, companyId);
        StringBuilder sqlSB = new StringBuilder("select employee_id from ");
        sqlSB.append(tableName).append(" where status=0 and leader=1 and department_id=").append(deparmentId);
        
        JSONObject userJSON = DAOUtil.executeQuery4FirstJSON(sqlSB.toString());
        if (userJSON == null)
        {
            return "";
        }
        log.warn("end!" + userJSON.getString("employee_id"));
        return userJSON.getString("employee_id");
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param finalDeparmentId 终止部门编号
     * @param starter 发起人
     * @return List<String>
     * @Description:获取多级部门负责人
     */
    public List<String> getDeparmentPrincipals(String companyId, String finalDeparmentId, String starter)
    {
        log.warn(String.format("parameters{args0:%s,args1:%s,args2:%s} start!", companyId, finalDeparmentId, starter));
        List<String> assignees = new ArrayList<>();
        String tableName = DAOUtil.getTableName(Constant.TABLE_DEPARTMENT_CENTER, companyId);
        StringBuilder sqlSB = new StringBuilder();
        sqlSB.append("select department_id,leader from ").append(tableName).append(" where status=0 and employee_id=").append(starter).append(" order by department_id");
        Map<String, Object> queryMap = DAOUtil.executeQuery4One(sqlSB.toString());
        Object id = queryMap.get("department_id");
        if (id != null)
        {
            int depId = Integer.parseInt(id.toString());
            int leader = Integer.parseInt(queryMap.get("leader").toString());
            String upDep = BusinessDAOUtil.getDepments(Long.parseLong(companyId), depId, 0);
            String[] dep = upDep.split(",");
            int size = dep.length;
            if (size > 0)
            {
                if (leader == 1 && size == 1)
                {
                    return assignees;
                }
                int level = size;
                if (leader == 1)
                {
                    level = level - 1;
                }
                if (level > size)
                {
                    level = size;
                }
                int flag = 0;
                StringBuilder depSB = new StringBuilder();
                if (Long.parseLong(finalDeparmentId) > 0)
                {
                    for (int i = 0; i < level; i++)
                    {
                        if (String.valueOf(finalDeparmentId).equals(dep[i]))
                        {
                            flag = i;
                            break;
                        }
                    }
                }
                for (int i = flag; i < level; i++)
                {
                    if (depSB.length() > 0)
                    {
                        depSB.append(",");
                    }
                    depSB.append(dep[i]);
                }
                if (depSB.length() > 0)
                {
                    sqlSB.setLength(0);
                    sqlSB.append("select employee_id from ").append(tableName).append(" where status=0 and leader =1 and department_id in(").append(depSB).append(
                        ") order by department_id desc");
                    List<Map<String, Object>> employeeMapLS = DAOUtil.executeQuery(sqlSB.toString());
                    for (Map<String, Object> map : employeeMapLS)
                    {
                        String employeeId = map.get("employee_id").toString();
                        assignees.add(employeeId);
                        log.warn("getDeparmentPrincipals employees :" + employeeId);
                    }
                    log.warn("getDeparmentPrincipals deparments:" + depSB + ",employees size:" + assignees.size());
                }
            }
        }
        
        log.warn("getDeparmentPrincipals employees size:" + assignees.size());
        log.warn("end!");
        return assignees;
    }
    
    /**
     * 
     * @param companyId 公司编号
     * @param roleId 角色编号
     * @return List<String>
     * @Description:根据角色获取员工
     */
    public List<String> getEmployees4Role(String companyId, String roleId)
    {
        log.warn(String.format("parameters{args0:%s,args1:%s} start!", companyId, roleId));
        List<String> assignees = new ArrayList<>();
        StringBuilder sqlSB = new StringBuilder();
        String tableName = DAOUtil.getTableName(Constant.TABLE_EMPLOYEE, companyId);
        sqlSB.append("select id from ").append(tableName).append(" where status=0 and del_status=0 and role_id=").append(roleId).append(" order by id");
        List<Map<String, Object>> employeeMapLS = DAOUtil.executeQuery(sqlSB.toString());
        for (Map<String, Object> map : employeeMapLS)
        {
            String employeeId = map.get("id").toString();
            assignees.add(employeeId);
            log.warn("getEmployees4Role employeeId:" + employeeId);
        }
        log.warn("getEmployees4Role role:" + roleId + ",employees size:" + assignees.size());
        log.warn("end!");
        return assignees;
    }
    
    /**
     * 
     * @param sql 分支条件（查询语句）
     * @param dataId 当前业务数据主键
     * @return boolean
     * @Description:判断是否符合分支条件
     */
    public boolean executeConditionExpression(String sql, String dataId)
    {
        if (StringUtils.isEmpty(sql))
        {
            return true;
        }
        String alias = "";
        sql = sql.replace(Constant.VAR_QUOTES, "'");
        StringBuilder sqlSB = new StringBuilder(sql);
        if (sql.contains(Constant.MAIN_TABLE_ALIAS))
        {
            alias = Constant.MAIN_TABLE_ALIAS + ".";
        }
        if (!sql.toLowerCase().contains("where"))
        {
            sqlSB.append(" where 1 = 1 ");
        }
        sqlSB.append(" and ").append(alias).append("id=").append(dataId);
        log.warn(String.format("parameters{args0:%s} start!", sqlSB.toString()));
        boolean flag = false;
        try
        {
            // Map<String, Object> resultMap =
            // DAOUtil.executeQuery4One(sqlSB.toString());
            JSONObject resultJSON = DAOUtil.executeQuery4FirstJSON(sqlSB.toString());// .executeQuery4One(sqlSB.toString());
            flag = null != resultJSON;
            log.warn("executeConditionExpression:" + sqlSB.toString() + ",hasData:" + flag);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        log.warn("end!");
        return flag;
    }
    
    /**
     * 
     * @param assignees 执行者集合(以','隔开)
     * @return List<String>
     * @Description:获取多实例任务的执行者集合
     */
    public List<String> getMultipleUser(String assignees)
    {
        log.warn(String.format("parameters{args0:%s} start!", assignees));
        List<String> users = new ArrayList<>();
        if (!StringUtils.isEmpty(assignees))
            for (String assignee : assignees.split(","))
            {
                users.add(assignee);
                log.warn("getMultipleUser assignee:" + assignee);
            }
        log.warn("getMultipleUser:" + assignees + "  size:" + users.size());
        log.warn("end!");
        return users;
    }
}