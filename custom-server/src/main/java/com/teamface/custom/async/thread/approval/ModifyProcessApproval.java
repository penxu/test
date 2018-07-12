package com.teamface.custom.async.thread.approval;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.teamface.common.util.dao.DAOUtil;

/**
 * @Title:
 * @Description:
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class ModifyProcessApproval extends Thread
{
    private static final Logger log = LogManager.getLogger(ModifyProcessApproval.class);
    
    private JSONObject reqJSON;
    
    public ModifyProcessApproval(JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
    }
    
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                // 审批申请表
                String processAttributeTable = DAOUtil.getTableName("process_approval", reqJSON.getLong("companyId"));
                // 构造sql
                StringBuilder modifySql = new StringBuilder();
                modifySql.append("update ").append(processAttributeTable).append(" set ");
                StringBuilder setValus = new StringBuilder();
                LinkedHashMap<String, String> jsonMap = JSON.parseObject(reqJSON.getString("reqJsonStr"), new TypeReference<LinkedHashMap<String, String>>()
                {
                });
                if (jsonMap.size() == 0)
                {
                    break;
                }
                for (Map.Entry<String, String> entry : jsonMap.entrySet())
                {
                    setValus.append(entry.getKey()).append(" = '").append(entry.getValue().replace("'", "''")).append("', ");
                }
                modifySql.append(setValus.substring(0, setValus.length() - 2));
                modifySql.append(" where module_bean = '").append(reqJSON.getString("moduleBean"));
                modifySql.append("' and approval_data_id = ").append(reqJSON.getLong("dataId"));
                // 执行sql
                DAOUtil.executeUpdate(modifySql.toString());
                break;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
