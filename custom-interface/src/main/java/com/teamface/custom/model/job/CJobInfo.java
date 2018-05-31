package com.teamface.custom.model.job;

import java.io.Serializable;

import com.teamface.custom.model.job.enums.BlockStrategyEnum;
import com.teamface.custom.model.job.enums.FailStrategyEnum;
import com.teamface.custom.model.job.enums.GlueTypeStrategyEnum;
import com.teamface.custom.model.job.enums.RouteStrategyEnum;

/**
 * 标准任务配置类
 * 
 * @Title:
 * @Description:
 * @Author:CZL
 * @Since:2018年5月7日
 * @Version:1.1.0
 */

public class CJobInfo implements Serializable
{
    
    /**
    *
    */
    
    private static final long serialVersionUID = 8054069894008112195L;
    
    private String jobGroupName = "custom-job-executor";// 执行器名称 custom-job-executor为默认
    
    private String jobCron; // 任务执行CRON表达式 【base on quartz】
    
    private String jobDesc; // 任务描述
    
    private String author; // 负责人
    
    private String alarmEmail; // 报警邮件，如果没有输入系统会使用默认的报警邮件
    
    private String executorHandler; // 执行器，任务Handler名称
    
    private String executorRouteStrategy = RouteStrategyEnum.ROUND.name(); // 执行器路由策略
    
    private String executorBlockStrategy = BlockStrategyEnum.SERIAL_EXECUTION.name(); // 阻塞处理策略
    
    private String executorFailStrategy = FailStrategyEnum.FAIL_RETRY.name(); // 失败处理策略
    
    private String glueType = GlueTypeStrategyEnum.BEAN.name(); // GLUE类型
                                                                // #com.xxl.job.core.glue.GlueTypeEnum
    
    public String getJobCron()
    {
        return jobCron;
    }
    
    public void setJobCron(String jobCron)
    {
        this.jobCron = jobCron;
    }
    
    public String getJobDesc()
    {
        return jobDesc;
    }
    
    public void setJobDesc(String jobDesc)
    {
        this.jobDesc = jobDesc;
    }
    
    public String getAuthor()
    {
        return author;
    }
    
    public void setAuthor(String author)
    {
        this.author = author;
    }
    
    public String getAlarmEmail()
    {
        return alarmEmail;
    }
    
    public void setAlarmEmail(String alarmEmail)
    {
        this.alarmEmail = alarmEmail;
    }
    
    public String getExecutorRouteStrategy()
    {
        return executorRouteStrategy;
    }
    
    public void setExecutorRouteStrategy(String executorRouteStrategy)
    {
        this.executorRouteStrategy = executorRouteStrategy;
    }
    
    public String getExecutorBlockStrategy()
    {
        return executorBlockStrategy;
    }
    
    public void setExecutorBlockStrategy(String executorBlockStrategy)
    {
        this.executorBlockStrategy = executorBlockStrategy;
    }
    
    public String getExecutorFailStrategy()
    {
        return executorFailStrategy;
    }
    
    public void setExecutorFailStrategy(String executorFailStrategy)
    {
        this.executorFailStrategy = executorFailStrategy;
    }
    
    public String getGlueType()
    {
        return glueType;
    }
    
    public void setGlueType(String glueType)
    {
        this.glueType = glueType;
    }
    
    public String getJobGroupName()
    {
        return jobGroupName;
    }
    
    public void setJobGroupName(String jobGroupName)
    {
        this.jobGroupName = jobGroupName;
    }
    
    public String getExecutorHandler()
    {
        return executorHandler;
    }
    
    public void setExecutorHandler(String executorHandler)
    {
        this.executorHandler = executorHandler;
    }
    
    @Override
    public String toString()
    {
        return "CJobInfo [jobGroupName=" + jobGroupName + ", jobCron=" + jobCron + ", jobDesc=" + jobDesc + ", author=" + author + ", alarmEmail=" + alarmEmail
            + ", executorHandler=" + executorHandler + ", executorRouteStrategy=" + executorRouteStrategy + ", executorBlockStrategy=" + executorBlockStrategy
            + ", executorFailStrategy=" + executorFailStrategy + ", glueType=" + glueType + "]";
    }
    
}
