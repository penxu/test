package com.teamface.custom.async.thread;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JedisClusterHelper;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * @Title:生成筛选条件
 * @Description:生成筛选条件线程
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class GenerateDataListFilterFields extends Thread
{
    private static final Logger log = LogManager.getLogger(GenerateDataListFilterFields.class);
    
    private String token;
    
    private Object obj;
    
    public GenerateDataListFilterFields(String token, Object obj)
    {
        this.obj = obj;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
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
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
