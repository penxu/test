package com.teamface.custom.service.quartz.job;

import java.util.Date;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/** 
* @Description:  
* @author: dsl 
* @date: 2017年11月13日 下午6:15:06 
* @version: 1.0 
*/

public class PublicPoolResignJob implements Job
{

    @Override
    public void execute(JobExecutionContext context)
        throws JobExecutionException
    {
        System.out.println("Has resigned  " + new Date());
      
    }

}

    