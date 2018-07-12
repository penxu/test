package com.teamface.custom.async.thread.approval;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.teamface.common.util.StringUtil;
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
public class SaveProcessApproval extends Thread
{
    private static final Logger log = LogManager.getLogger(SaveProcessApproval.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public SaveProcessApproval(String token, JSONObject reqJSON)
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
            
            // 保存审批记录
            String processFlowTable = DAOUtil.getTableName("process_approval", companyId);
            StringBuilder insertSql = new StringBuilder();
            insertSql.append("insert into ").append(processFlowTable).append("(");
            StringBuilder keys = new StringBuilder();
            StringBuilder vals = new StringBuilder();
            
            LinkedHashMap<String, String> jsonMap = JSON.parseObject(reqJSON.toString(), new TypeReference<LinkedHashMap<String, String>>()
            {
            });
            for (Map.Entry<String, String> entry : jsonMap.entrySet())
            {
                if (!StringUtil.isEmpty(entry.getValue()))
                {
                    keys.append(entry.getKey()).append(", ");
                    vals.append("'").append(entry.getValue()).append("', ");
                }
            }
            insertSql.append(keys.substring(0, keys.length() - 2)).append(") values(");
            insertSql.append(vals.substring(0, vals.length() - 2)).append(")");
            DAOUtil.executeUpdate(insertSql.toString());
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
