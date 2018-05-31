package com.teamface.common.util;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import com.teamface.common.constant.Constant;

/**
 * @Description: 任务管理（线程池）
 * @author: ZZH
 * @date: 2017年12月1日 下午4:25:42
 * @version: 1.0
 */

public class JobManager
{
    private int threadCount = 0;
    
    private ThreadPoolExecutor exe;
    
    private int maxrnum;
    
    private int waitrnum;
    
    private String queueName;
    
    private static JobManager instance = null;
    
    public static synchronized JobManager getInstance()
    {
        if (instance == null)
        {
            instance = new JobManager();
        }
        return instance;
    }
    
    public static JobManager getNewInstance()
    {
        return new JobManager();
    }
    
    private JobManager()
    {
        maxrnum = Constant.JOB_MAX_COUNT;
        waitrnum = Constant.JOB_WAIT_COUNT;
        ThreadFactory threadFactory = new ThreadFactory()
        {
            public Thread newThread(Runnable r)
            {
                Thread t = new Thread(r);
                threadCount = threadCount + 1;
                String name = "JobManagerThread" + threadCount;
                t.setName(name);
                return t;
            }
            
        };
        
        exe = new ThreadPoolExecutor(maxrnum, maxrnum + waitrnum, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(waitrnum), threadFactory);
    }
    
    public boolean submitJob(Thread t)
    {
        int waitCount = exe.getQueue().size();
        if (waitCount < waitrnum)
        {
            queueName = t.getClass().getName();
            exe.execute(t);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public void shutdown()
    {
        exe.shutdown();
    }
    
    public int getRunJobNums()
    {
        return exe.getPoolSize();
    }
    
    public int getWaitJobNums()
    {
        return exe.getQueue().size();
    }
    
    public int getActiveJobNums()
    {
        return exe.getActiveCount();
    }
    
    public String getQueueName()
    {
        return queueName;
    }
}