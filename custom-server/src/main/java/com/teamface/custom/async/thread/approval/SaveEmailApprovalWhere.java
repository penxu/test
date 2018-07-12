package com.teamface.custom.async.thread.approval;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * @Title:
 * @Description:
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class SaveEmailApprovalWhere extends Thread
{
    private static final Logger log = LogManager.getLogger(SaveEmailApprovalWhere.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public SaveEmailApprovalWhere(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            // 解析token
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            
            // 节点分支条件表
            String whereTable = DAOUtil.getTableName("email_node_branch_where", companyId);
            // 节点分支规则表
            String conditionsTable = DAOUtil.getTableName("email_node_branch_condistion", companyId);
            
            JSONArray reqJSONArray = reqJSON.getJSONArray("reqJSONArray");
            for (Object reqJSONObj : reqJSONArray)
            {
                JSONObject reqJSON = (JSONObject)reqJSONObj;
                String processKey = reqJSON.getString("processKey");
                StringBuilder executeSql = new StringBuilder();
                executeSql.append("delete from ").append(conditionsTable);
                executeSql.append(" where email_where_id in(select id from ").append(whereTable);
                executeSql.append(" where process_attribute_key = '").append(processKey).append("')");
                DAOUtil.executeUpdate(executeSql.toString());
                
                executeSql.setLength(0);
                executeSql.append("delete from ").append(whereTable);
                executeSql.append(" where process_attribute_key = '").append(processKey).append("'");
                DAOUtil.executeUpdate(executeSql.toString());
                
                executeSql.setLength(0);
                Long whereId = BusinessDAOUtil.getNextval4Table("email_node_branch_where", companyId.toString());
                executeSql.append("insert into ");
                executeSql.append(whereTable);
                executeSql.append("(id, process_attribute_key, source_ref, target_ref, create_by, create_time) values(");
                executeSql.append(whereId).append(", '");
                executeSql.append(processKey).append("', '");
                executeSql.append(Constant.PROCESS_FIELD_FIRST_TASK).append("', '");
                executeSql.append(reqJSON.getString("targetRef")).append("', ");
                executeSql.append(employeeId).append(", ");
                executeSql.append(System.currentTimeMillis()).append(")");
                DAOUtil.executeUpdate(executeSql.toString());
                
                JSONArray relevanceWhere = reqJSON.getJSONArray("relevanceWhere");
                List<Object[]> batchValues = new ArrayList<>();
                for (Object relevanceWhereObj : relevanceWhere)
                {
                    JSONObject relevanceWhereJSON = (JSONObject)relevanceWhereObj;
                    List<Object> relevanceWhereList = new ArrayList<>();
                    relevanceWhereList.add(whereId);
                    relevanceWhereList.add(relevanceWhereJSON.getString("field_value"));
                    relevanceWhereList.add(relevanceWhereJSON.getString("field_label"));
                    relevanceWhereList.add(relevanceWhereJSON.getString("operator_value"));
                    relevanceWhereList.add(relevanceWhereJSON.getString("operator_label"));
                    
                    // 可见性
                    JSONArray visibilityArr = relevanceWhereJSON.getJSONArray("result_value");
                    StringBuilder userIds = new StringBuilder();
                    StringBuilder roleIds = new StringBuilder();
                    StringBuilder departmentIds = new StringBuilder();
                    boolean companyFlag = false;
                    for (Object shareObj : visibilityArr)
                    {
                        JSONObject shareJson = (JSONObject)shareObj;
                        String shareId = shareJson.getString("id");
                        String shareType = shareJson.getString("type");// 0部门，1人员，2角色，4全公司
                        if (shareType.equals("0"))
                        {
                            departmentIds.append(shareId).append(",");
                        }
                        else if (shareType.equals("1"))
                        {
                            userIds.append(shareId).append(",");
                        }
                        else if (shareType.equals("2"))
                        {
                            roleIds.append(shareId).append(",");
                        }
                        else if (shareType.equals("4"))
                        {
                            companyFlag = true;
                        }
                    }
                    relevanceWhereList.add(userIds.length() == 0 ? "" : userIds.substring(0, userIds.lastIndexOf(",")));
                    relevanceWhereList.add(roleIds.length() == 0 ? "" : roleIds.substring(0, roleIds.lastIndexOf(",")));
                    relevanceWhereList.add(departmentIds.length() == 0 ? "" : departmentIds.substring(0, departmentIds.lastIndexOf(",")));
                    relevanceWhereList.add(companyFlag ? "1" : "0");
                    relevanceWhereList.add(employeeId);
                    relevanceWhereList.add(System.currentTimeMillis());
                    batchValues.add(relevanceWhereList.toArray());
                }
                executeSql.setLength(0);
                // 批量插入每个节点的高级条件
                executeSql.append("insert into ");
                executeSql.append(conditionsTable);
                executeSql.append(
                    "(email_where_id, field_name, field_label, operator_name, operator_label, user_ids, role_ids, department_ids, company_id, create_by, create_time) values(?,?,?,?,?,?,?,?,?,?,?)");
                DAOUtil.executeBatchUpdate(executeSql.toString(), batchValues);
                break;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
