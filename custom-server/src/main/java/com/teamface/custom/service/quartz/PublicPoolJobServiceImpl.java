package com.teamface.custom.service.quartz;

import java.util.HashMap;
import java.util.Map;

import org.quartz.Job;
import org.springframework.stereotype.Service;

import com.teamface.common.util.QuartzManager;
import com.teamface.custom.service.quartz.job.PublicCompany;
import com.teamface.custom.service.quartz.job.PublicInvite;
import com.teamface.custom.service.quartz.job.PublicPoolResignJob;
import com.teamface.custom.service.quartz.job.PublicRecyclePoolJob;
import com.teamface.custom.service.quartz.job.PulicCompanyStart;

/**
 * 
 * @Title:
 * @Description:自动标识颜色
 * @Author:liupan
 * @Since:2017年11月27日
 * @Version:1.1.0
 */
@Service("publicPoolJobService")
public class PublicPoolJobServiceImpl implements PublicPoolJobService
{
    
    /**
     * 自动标识颜色
     */
    @Override
    public boolean publicPoolRecycle(String companyId)
    {
        String jobName = "PUBLIC_RECYCLE" + companyId;
        String triggerName = "PUBLIC_RECYCLE_TRIGGER" + companyId;
        Map<String, Object> parameter = new HashMap<>();
        parameter.put("reqJsonStr", "jsonStr");
        parameter.put("companyId", companyId);
        QuartzManager.getInstance().addJob(jobName, triggerName, PublicRecyclePoolJob.class, "0/5 * *  * * ?", parameter);
        return true;
    }
    
    /**
     * 邀请码定时器
     */
    @Override
    public boolean publicInvite()
    {
        String jobName = "PUBLIC_RESIGN";
        String triggerName = "PUBLIC_RESIGN_TRIGGER";
        QuartzManager.getInstance().addJob(jobName, triggerName, PublicInvite.class, "0 1/2 12-18 * * ?");
        return true;
    }
    
    /**
     * 公司套餐结束定时器
     */
    @Override
    public boolean publicCompanyEndTime()
    {
        String jobName = "PUBLIC_COMPANY_END_TIME";
        String triggerName = "PUBLIC_COMPANY_END_TIME_TRIGGER";
        QuartzManager.getInstance().addJob(jobName, triggerName, PublicCompany.class, "0 1/2 12-18 * * ?");
        return true;
    }
    
    /**
     * 公司套餐开始定时器
     */
    @Override
    public boolean publicCompanyStartTime()
    {
        String jobName = "PUBLIC_COMPANY_START_TIME";
        String triggerName = "PUBLIC_COMPANY_START_TIME_TRIGGER";
        QuartzManager.getInstance().addJob(jobName, triggerName, PulicCompanyStart.class, "0 1/2 12-18 * * ?");
        return true;
    }
    
}
