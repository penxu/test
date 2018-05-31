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
 * 
*@Title:
*@Description:公司套餐结束时间定时器 
*@Author:liupan
*@Since:2018年4月8日
*@Version:1.1.0
 */
public class PublicCompany implements Job
{
    
    private static final Logger LOG = LogManager.getLogger(PublicCompany.class);
    
    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException
    {
        String companyTable = DAOUtil.getTableName("company", "");
        String formalTable = DAOUtil.getTableName("formal_user", "");
        StringBuilder sql = new StringBuilder("select * from ").append(formalTable).append(" where end_time < ").append(System.currentTimeMillis());
        List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(sql.toString());
        if (jsonObject.size() > 0)
        {
            List<List<Object>> batchValues = new ArrayList<>();
            for (int i = 0; i < jsonObject.size(); i++)
            {
                List<Object> model = new ArrayList<>();
                model.add(Constant.CURRENCY_ONE);
                model.add(jsonObject.get(i).getLong("company_id"));
                batchValues.add(model);
            }
            
            StringBuilder editBilder = new StringBuilder("update ").append(companyTable).append(" set status =  ? where id = ?");
            int count = DAOUtil.executeUpdate(editBilder.toString(), batchValues, 1000);
            LOG.debug("定时器公司套餐" + count);
        }
        
    }
    
}
