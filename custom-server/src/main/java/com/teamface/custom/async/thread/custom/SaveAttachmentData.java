package com.teamface.custom.async.thread.custom;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.custom.service.library.FileLibraryAppService;

/**
 * @Title:
 * @Description:保存-附件、图片
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class SaveAttachmentData extends Thread
{
    private static final Logger log = LogManager.getLogger(SaveAttachmentData.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public SaveAttachmentData(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
            FileLibraryAppService fileLibraryAppService = wac.getBean(FileLibraryAppService.class);
            
            JSONObject saveJson = reqJSON.getJSONObject("saveJson");
            String companyId = reqJSON.getString("companyId");
            Long relationId = reqJSON.getLong("relationId");
            
            List<String> attachmentKeys = new ArrayList<String>();
            Set<String> keys = saveJson.keySet();
            for (String key : keys)
            {
                if (key.contains("picture") || key.contains("attachment"))
                {
                    if (!StringUtil.isEmpty(saveJson.get(key)))
                    {
                        attachmentKeys.add(key);
                    }
                }
            }
            
            if (!attachmentKeys.isEmpty())
            {
                List<Object[]> batchValues = new ArrayList<>();
                List<JSONObject> attachments = new ArrayList<JSONObject>();
                for (String key : attachmentKeys)
                {
                    JSONArray attachmentArr = saveJson.getJSONArray(key);
                    if (attachmentArr.size() > 0)
                    {
                        for (Object attachmentObj : attachmentArr)
                        {
                            JSONObject attachmentJson = (JSONObject)attachmentObj;
                            List<Object> attachmentValues = new ArrayList<Object>();
                            attachmentValues.add(relationId);
                            attachmentValues.add(attachmentJson.getString("file_name"));
                            attachmentValues.add(attachmentJson.getString("file_type"));
                            attachmentValues.add(attachmentJson.getLong("file_size"));
                            attachmentValues.add(attachmentJson.getString("file_url"));
                            attachmentValues.add(key);
                            attachmentValues.add(attachmentJson.getInteger("serial_number"));
                            attachmentValues.add(attachmentJson.getString("upload_by"));
                            attachmentValues.add(attachmentJson.getLong("upload_time"));
                            batchValues.add(attachmentValues.toArray());
                            JSONObject pobj = new JSONObject();
                            pobj.put("id", relationId);
                            pobj.put("url", attachmentJson.getString("file_url"));
                            attachments.add(pobj);
                        }
                    }
                }
                // 先删除后增加
                String attachmentTable = DAOUtil.getTableName("attachment", companyId);
                StringBuilder batchInsertSql = new StringBuilder();
                batchInsertSql.append(" delete from ").append(attachmentTable).append(" where data_id=").append(relationId);
                log.info(batchInsertSql);
                DAOUtil.executeUpdate(batchInsertSql.toString());
                batchInsertSql.setLength(0);
                batchInsertSql.append("insert into ");
                batchInsertSql.append(attachmentTable);
                batchInsertSql.append(
                    "(data_id, file_name, file_type, file_size, file_url, original_file_name, serial_number, upload_by, upload_time) values(?, ?, ?, ?, ?, ?, ?, ?, ?)");
                log.info(batchInsertSql);
                DAOUtil.executeBatchUpdate(batchInsertSql.toString(), batchValues);
                
                fileLibraryAppService.editModuleDataId(token, attachments);
            }
            else
            {
                // 删除图片
                String attachmentTable = DAOUtil.getTableName("attachment", companyId);
                StringBuilder batchInsertSql = new StringBuilder();
                batchInsertSql.append(" delete from ").append(attachmentTable).append(" where data_id=").append(relationId);
                DAOUtil.executeUpdate(batchInsertSql.toString());
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            e.printStackTrace();
        }
    }
}
