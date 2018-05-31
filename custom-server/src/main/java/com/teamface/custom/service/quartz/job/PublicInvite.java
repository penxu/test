package com.teamface.custom.service.quartz.job;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.DAOUtil;

/**
 * 邀请码
 * 
 * @Title:
 * @Description:  超时邀请码修改状态
 * @Author:
 * @Since:2018年2月9日
 * @Version:1.1.0
 */
public class PublicInvite implements Job
{
    private static final Logger LOG = LogManager.getLogger(PublicInvite.class);
    
    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException
    {
        String inviteTable = DAOUtil.getTableName("invite", "");
        StringBuilder sql = new StringBuilder("select * from ").append(inviteTable).append(" where end_time < " ).append( System.currentTimeMillis());
        List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(sql.toString());
        if(jsonObject.size()>0) {
            List<List<Object>> batchValues = new ArrayList<>();
            for (int i = 0; i < jsonObject.size(); i++)
            {
                List<Object> model = new ArrayList<>();
                model.add(Constant.CURRENCY_TWO);
                model.add(jsonObject.get(i).getLong("id"));
                batchValues.add(model);
            }
            
            StringBuilder editBilder = new StringBuilder("update ").append(inviteTable).append("  set status =  ? where id = ?");
            int count = DAOUtil.executeUpdate(editBilder.toString(), batchValues, 1000);
            LOG.debug("定时器邀请码"+count);
        }
        
    }
    
}
