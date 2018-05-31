package com.teamface.custom.async;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * @Description:审批异步推送
 * @author: caojianhua
 * @date: 2018年1月31日 下午2:05:04
 * @version: 1.0
 */

public class ReportAsyncHandle
{
    private static Logger log = Logger.getLogger(ReportAsyncHandle.class);
    
    private Object obj;
    
    private String token;
    
    public ReportAsyncHandle(Object obj, String token)
    {
        this.obj = obj;
        this.token = token;
    }
    
    /**
     * @Description:异步方法结构
     */
    public void testThreadPool()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                System.err.println("添加业务实现");
            }
        });
    }
    
    /**
     * @Description:保存点击查看报表记录
     */
    public void saveClickReport()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject clickJson = (JSONObject)obj;
                    String clickTable = DAOUtil.getTableName("report_click_hist", clickJson.getString("companyId"));
                    StringBuilder insertSql = new StringBuilder();
                    insertSql.append("insert into ").append(clickTable);
                    insertSql.append("(report_id, create_by, create_time) values(");
                    insertSql.append(clickJson.getString("reportId")).append(", ");
                    insertSql.append(clickJson.getString("createBy")).append(", ");
                    insertSql.append(System.currentTimeMillis()).append(")");
                    
                    int insertResult = DAOUtil.executeUpdate(insertSql.toString());
                    if (insertResult < 1)
                    {
                        log.error("Async insert to click report error!!!");
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * 保存共享报表记录
     * 
     * @Description:
     */
    public void saveShareReport()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                try
                {
                    JSONObject shareJson = (JSONObject)obj;
                    String companyId = shareJson.getString("companyId");
                    
                    JSONArray shareArr = shareJson.getJSONArray("shareObj");
                    // 共享员工汇总
                    StringBuilder shareUser = new StringBuilder();
                    for (Object shareObj : shareArr)
                    {
                        JSONObject tmpShare = (JSONObject)shareObj;
                        String shareUserType = tmpShare.getString("type");// 0部门，1人员，2角色
                        String shareUserId = tmpShare.getString("id");
                        if (shareUserType.equals("0"))
                        {
                            String departmentTable = DAOUtil.getTableName("department_center", companyId);
                            StringBuilder querySql = new StringBuilder();
                            querySql.append("select string_agg(employee_id,',') as employee_id from ").append(departmentTable);
                            querySql.append(" where department_id = ").append(shareUserId);
                            JSONObject employeeJson = DAOUtil.executeQuery4FirstJSON(querySql.toString());
                            if (null != employeeJson && !employeeJson.getString("employee_id").isEmpty())
                            {
                                shareUser.append(employeeJson.getString("employee_id")).append(",");
                            }
                        }
                        else if (shareUserType.equals("1"))
                        {
                            shareUser.append(shareUserId).append(",");
                        }
                        else if (shareUserType.equals("2"))
                        {
                            String employeeTable = DAOUtil.getTableName("employee", companyId);
                            StringBuilder querySql = new StringBuilder();
                            querySql.append("select string_agg(id,',') as employee_id from ").append(employeeTable);
                            querySql.append(" where role_id = ").append(shareUserId);
                            
                            JSONObject employeeJson = DAOUtil.executeQuery4FirstJSON(querySql.toString());
                            if (null != employeeJson && !employeeJson.getString("employee_id").isEmpty())
                            {
                                shareUser.append(employeeJson.getString("employee_id")).append(",");
                            }
                        }
                    }
                    
                    // 生成共享数据
                    String shareTable = DAOUtil.getTableName("report_share", companyId);
                    StringBuilder insertSql = new StringBuilder();
                    insertSql.append("insert into ").append(shareTable);
                    insertSql.append("(report_id, share_user, create_by, create_time) values(");
                    insertSql.append(shareJson.getString("reportId")).append(", '");
                    insertSql.append(shareUser.substring(0, shareUser.lastIndexOf(","))).append("', ");
                    insertSql.append(shareJson.getString("createBy")).append(", ");
                    insertSql.append(System.currentTimeMillis()).append(")");
                    
                    int insertResult = DAOUtil.executeUpdate(insertSql.toString());
                    if (insertResult < 1)
                    {
                        log.error("Async insert to share error!!!");
                    }
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    e.printStackTrace();
                }
            }
        });
    }
    
    /**
     * @Description:生成筛选条件
     */
    public void generateDataListFilterFields()
    {
        ExecutorThreadPool.getInstance().execute(new Runnable()
        {
            @Override
            public void run()
            {
                @SuppressWarnings("unchecked")
                List<JSONObject> dataList = (List<JSONObject>)obj;
                List<JSONObject> result = new ArrayList<JSONObject>();
                List<JSONObject> dataSourceList = new ArrayList<JSONObject>();
                Map<String, String> duplicateKeys = new HashMap<String, String>();
                StringBuilder createIds = new StringBuilder();
                StringBuilder modifyIds = new StringBuilder();
                for (JSONObject dataJSON : dataList)
                {
                    String dataSourceName = dataJSON.getString("data_source_name");
                    String createId = dataJSON.getString("create_by_id");
                    String modifyId = dataJSON.getString("modify_by_id");
                    
                    // 数据源去重
                    if (!StringUtil.isEmpty(dataSourceName) && !duplicateKeys.containsKey(dataSourceName))
                    {
                        JSONObject dataSourceJSON = new JSONObject();
                        dataSourceJSON.put("label", dataJSON.getString("data_source_label"));
                        dataSourceJSON.put("value", dataSourceName);
                        dataSourceList.add(dataSourceJSON);
                        duplicateKeys.put(dataSourceName, "Y");
                    }
                    
                    // 创建人去重
                    if (!StringUtil.isEmpty(createId) && !duplicateKeys.containsKey("cId_".concat(createId)))
                    {
                        createIds.append(createId).append(",");
                        duplicateKeys.put("cId_".concat(createId), "Y");
                    }
                    
                    // 修改人去重
                    if (!StringUtil.isEmpty(modifyId) && !duplicateKeys.containsKey("mId_".concat(modifyId)))
                    {
                        modifyIds.append(modifyId).append(",");
                        duplicateKeys.put("mId_".concat(modifyId), "Y");
                    }
                }
                
                // 解析token
                InfoVo info = TokenMgr.obtainInfo(token);
                long companyId = info.getCompanyId();
                
                StringBuilder querySql = new StringBuilder();
                JSONObject reportName = new JSONObject();
                reportName.put("id", "reportLabel");
                reportName.put("name", "报表名称");
                reportName.put("type", "text");
                result.add(reportName);
                
                JSONObject dataSourceName = new JSONObject();
                dataSourceName.put("id", "dataSourceName");
                dataSourceName.put("name", "数据源");
                dataSourceName.put("type", "picklist");
                dataSourceName.put("entrys", dataSourceList);
                result.add(dataSourceName);
                
                if (createIds.length() != 0)
                {
                    querySql.append("select t1.id, t1.employee_name as \"name\", t1.picture, t2.name as post_name from employee_").append(companyId);
                    querySql.append(" t1, post_").append(companyId);
                    querySql.append(" t2 where t1.id in(").append(createIds.substring(0, createIds.lastIndexOf(",")));
                    querySql.append(" ) and t1.post_id = t2.id");
                }
                JSONObject createBy = new JSONObject();
                createBy.put("id", "createBy");
                createBy.put("name", "创建人");
                createBy.put("type", "personnel");
                createBy.put("member", querySql.length() == 0 ? new ArrayList<>() : DAOUtil.executeQuery4JSON(querySql.toString()));
                result.add(createBy);
                
                JSONObject createTime = new JSONObject();
                createTime.put("id", "createTime");
                createTime.put("name", "创建时间");
                createTime.put("type", "datetime");
                result.add(createTime);
                
                querySql = new StringBuilder();
                if (modifyIds.length() != 0)
                {
                    querySql.append("select t1.id, t1.employee_name as name, t1.picture, t2.name as post_name from employee_").append(companyId);
                    querySql.append(" t1, post_").append(companyId);
                    querySql.append(" t2 where t1.id in(").append(modifyIds.substring(0, modifyIds.lastIndexOf(",")));
                    querySql.append(" ) and t1.post_id = t2.id");
                }
                JSONObject modifyBy = new JSONObject();
                modifyBy.put("id", "modifyBy");
                modifyBy.put("name", "修改人");
                modifyBy.put("type", "personnel");
                modifyBy.put("member", querySql.length() == 0 ? new ArrayList<>() : DAOUtil.executeQuery4JSON(querySql.toString()));
                result.add(modifyBy);
                
                JSONObject modifyTime = new JSONObject();
                modifyTime.put("id", "modifyTime");
                modifyTime.put("name", "修改时间");
                modifyTime.put("type", "datetime");
                result.add(modifyTime);
                JedisClusterHelper.set(new StringBuilder("report_datalist_filterfields_").append(companyId).append("_").append(info.getEmployeeId()).toString(), result);
            }
        });
    }
}
