package com.teamface.custom.async.thread.approval;

import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.custom.async.AsyncHandle;
import com.teamface.custom.async.thread.custom.SaveAttachmentData;

/**
 * @Title:异步修改业务数据
 * @Description:异步修改业务数据线程
 * @Author:cjh
 * @Since:2018年6月12日
 * @Version:1.1.0
 */
public class ModifyBusinessData extends Thread
{
    private static final Logger log = LogManager.getLogger(ModifyBusinessData.class);
    
    private String token;
    
    private JSONObject reqJSON;
    
    public ModifyBusinessData(String token, JSONObject reqJSON)
    {
        this.reqJSON = reqJSON;
        this.token = token;
    }
    
    @Override
    public void run()
    {
        try
        {
            while (true)
            {
                String modifyDataObj = reqJSON.getString("modifyDataObj");
                String moduleBean = reqJSON.getString("moduleBean");
                long companyId = reqJSON.getLongValue("companyId");
                long dataId = reqJSON.getLongValue("dataId");
                
                if (moduleBean.equals(Constant.PROCESS_MAIL_BOX_SCOPE))
                {
                    break;
                }
                String businessTable = DAOUtil.getTableName(moduleBean, companyId);
                StringBuilder modifySql = new StringBuilder();
                modifySql.append("update ").append(businessTable).append(" set ");
                StringBuilder setValus = new StringBuilder();
                JSONObject attImgJSON = new JSONObject();
                LinkedHashMap<String, String> jsonMap = JSON.parseObject(modifyDataObj, new TypeReference<LinkedHashMap<String, String>>()
                {
                });
                for (Map.Entry<String, String> entry : jsonMap.entrySet())
                {
                    String key = entry.getKey();
                    if (key.startsWith(Constant.TYPE_PICTURE) || key.startsWith(Constant.TYPE_ATTACHMENT))
                    {
                        attImgJSON.put(key, entry.getValue());
                        continue;
                    }
                    setValus.append(key).append(" = '").append(entry.getValue()).append("', ");
                }
                if (setValus.length() == 0)
                {
                    break;
                }
                modifySql.append(setValus.substring(0, setValus.length() - 2)).append(" where id = ").append(dataId).append("");
                log.debug("end !");
                // 修改业务数据
                DAOUtil.executeUpdate(modifySql.toString());
                // 修改附件、图片数据
                if (attImgJSON.size() > 0)
                {
                    JSONObject attachmentReqJSON = new JSONObject();
                    attachmentReqJSON.put("saveJson", attImgJSON);
                    attachmentReqJSON.put("companyId", companyId);
                    attachmentReqJSON.put("relationId", dataId);
                    AsyncHandle approvalHandle = new AsyncHandle();
                    SaveAttachmentData sad = new SaveAttachmentData(token, attachmentReqJSON);
                    sad.setName("SaveAttachmentData-Thread");
                    approvalHandle.commitJob(sad);
                }
                break;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
}
