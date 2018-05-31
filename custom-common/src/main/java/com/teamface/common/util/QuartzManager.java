package com.teamface.common.util;

import java.util.Map;

import org.apache.log4j.Logger;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;

/**
 * @Description: Quartz调度管理器
 * @author: ZZH
 * @date: 2017年11月13日 上午10:34:34
 * @version: 1.0
 */

public class QuartzManager
{
    
    private static final Logger log = Logger.getLogger(QuartzManager.class);
    
    private static final String JOB_GROUP_NAME = "JOBGROUP_NAME";
    
    private static final String TRIGGER_GROUP_NAME = "TRIGGERGROUP_NAME";
    
    private static QuartzManager instance = null;
    
    private Scheduler scheduler = null;
    
    public static synchronized QuartzManager getInstance()
    {
        if (instance == null)
        {
            instance = new QuartzManager();
        }
        return instance;
    }
    
    private QuartzManager()
    {
        try
        {
            scheduler = (Scheduler)SpringContextUtil.getBean("schedulerFactory");
        }
        catch (Exception e)
        {
            log.error(e.toString());
        }
    }
    
    /**
     * @Description: 添加一个定时任务
     *               
     * @param jobName 任务名
     * @param triggerName 触发器名
     * @param jobClass 任务
     * @param cron 时间设置，参考quartz说明文档
     */
    public void addJob(String jobName, String triggerName, Class<? extends Job> jobClass, String cron)
    {
        addJob(jobName, JOB_GROUP_NAME, triggerName, TRIGGER_GROUP_NAME, jobClass, cron);
    }
    
    /**
     * 
     * @param jobName 任务名
     * @param triggerName 触发器名
     * @param jobClass 任务
     * @param cron 时间设置，参考quartz说明文档
     * @param para 定时任务传参
     * @Description:添加一个定时任务
     */
    public void addJob(String jobName, String triggerName, Class<? extends Job> jobClass, String cron, Map<String, Object> para)
    {
        addJob(jobName, JOB_GROUP_NAME, triggerName, TRIGGER_GROUP_NAME, jobClass, cron, para);
    }
    
    /**
     * @Description: 修改一个任务的触发时间
     *               
     * @param jobName
     * @param triggerName 触发器名
     * @param cron 时间设置，参考quartz说明文档
     */
    public void modifyJobTime(String jobName, String triggerName, String cron)
    {
        modifyJobTime(jobName, JOB_GROUP_NAME, triggerName, TRIGGER_GROUP_NAME, cron);
    }
    
    /**
     * @Description: 移除一个任务
     *               
     * @param jobName
     * @param triggerName
     */
    public void removeJob(String jobName, String triggerName)
    {
        removeJob(jobName, JOB_GROUP_NAME, triggerName, TRIGGER_GROUP_NAME);
    }
    
    /**
     * @Description: 添加一个定时任务
     *               
     * @param jobName 任务名
     * @param jobGroupName 任务组名
     * @param triggerName 触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass 任务
     * @param cron 时间设置，参考quartz说明文档
     */
    public void addJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class<? extends Job> jobClass, String cron)
    {
        try
        {
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            // 创建Trigger对象
            CronTrigger trigger = (CronTrigger)triggerBuilder.build();
            
            // 调度容器设置JobDetail和Trigger
            scheduler.scheduleJob(jobDetail, trigger);
            
            // 启动
            if (!scheduler.isShutdown())
            {
                scheduler.start();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param jobName 任务
     * @param jobGroupName 任务组名
     * @param triggerName 触发器名
     * @param triggerGroupName 触发器组名
     * @param jobClass 任务
     * @param cron 时间设置，参考quartz说明文档
     * @param parameter 参数传递
     * @Description:添加一个定时任务
     */
    public void addJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName, Class<? extends Job> jobClass, String cron, Map<String, Object> parameter)
    {
        try
        {
            // 任务名，任务组，任务执行类
            JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(jobName, jobGroupName).build();
            // 定时任务参数传递
            jobDetail.getJobDataMap().putAll(parameter);
            // 触发器
            TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
            // 触发器名,触发器组
            triggerBuilder.withIdentity(triggerName, triggerGroupName);
            triggerBuilder.startNow();
            // 触发器时间设定
            triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
            // 创建Trigger对象
            CronTrigger trigger = (CronTrigger)triggerBuilder.build();
            
            // 调度容器设置JobDetail和Trigger
            scheduler.scheduleJob(jobDetail, trigger);
            
            // 启动
            if (!scheduler.isShutdown())
            {
                scheduler.start();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @Description: 修改一个任务的触发时间
     *               
     * @param jobName
     * @param jobGroupName
     * @param triggerName 触发器名
     * @param triggerGroupName 触发器组名
     * @param cron 时间设置，参考quartz说明文档
     */
    public void modifyJobTime(String jobName, String jobGroupName, String triggerName, String triggerGroupName, String cron)
    {
        try
        {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            CronTrigger trigger = (CronTrigger)scheduler.getTrigger(triggerKey);
            if (trigger == null)
            {
                return;
            }
            
            String oldTime = trigger.getCronExpression();
            if (!oldTime.equalsIgnoreCase(cron))
            {
                // 触发器
                TriggerBuilder<Trigger> triggerBuilder = TriggerBuilder.newTrigger();
                // 触发器名,触发器组
                triggerBuilder.withIdentity(triggerName, triggerGroupName);
                triggerBuilder.startNow();
                // 触发器时间设定
                triggerBuilder.withSchedule(CronScheduleBuilder.cronSchedule(cron));
                // 创建Trigger对象
                trigger = (CronTrigger)triggerBuilder.build();
                // 修改一个任务的触发时间
                scheduler.rescheduleJob(triggerKey, trigger);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @Description: 移除一个任务
     *               
     * @param jobName
     * @param jobGroupName
     * @param triggerName
     * @param triggerGroupName
     */
    public void removeJob(String jobName, String jobGroupName, String triggerName, String triggerGroupName)
    {
        try
        {
            TriggerKey triggerKey = TriggerKey.triggerKey(triggerName, triggerGroupName);
            
            scheduler.pauseTrigger(triggerKey);// 停止触发器
            scheduler.unscheduleJob(triggerKey);// 移除触发器
            scheduler.deleteJob(JobKey.jobKey(jobName, jobGroupName));// 删除任务
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param jobName 定时任务名称
     * @param groupName 定时任务组名
     * @Description: 暂停定时任务
     */
    public void pauseJob(String jobName, String groupName)
    {
        try
        {
            JobKey jobKey = JobKey.jobKey(jobName, null == groupName ? JOB_GROUP_NAME : groupName);
            scheduler.pauseJob(jobKey);
        }
        catch (SchedulerException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param jobName 定时任务名称
     * @param groupName 定时任务组名
     * @Description: 启动暂停的定时任务
     */
    public void resumeJob(String jobName, String groupName)
    {
        try
        {
            JobKey jobKey = JobKey.jobKey(jobName, null == groupName ? JOB_GROUP_NAME : groupName);
            scheduler.resumeJob(jobKey);
        }
        catch (SchedulerException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 
     * @param jobName 定时任务名称
     * @param groupName 定时任务组名
     * @Description:
     */
    public boolean checkJobExistence(String jobName, String groupName)
    {
        try
        {
            JobKey jobKey = JobKey.jobKey(jobName, null == groupName ? JOB_GROUP_NAME : groupName);
            return scheduler.checkExists(jobKey);
        }
        catch (SchedulerException e)
        {
            log.error(e.getMessage(), e);
        }
        return false;
    }
    
    /**
     * @Description:启动所有定时任务
     */
    public void startJobs()
    {
        try
        {
            scheduler.start();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @Description:关闭所有定时任务
     */
    public void shutdownJobs()
    {
        try
        {
            if (!scheduler.isShutdown())
            {
                scheduler.shutdown();
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
}