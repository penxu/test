package com.teamface.custom.async;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.teamface.common.util.JobManager;

/**
 * @Description:审批异步处理器
 * @author: caojianhua
 * @date: 2018年1月31日 下午2:05:04
 * @version: 1.0
 */

public class AsyncHandle
{
    private static final Logger log = LogManager.getLogger(AsyncHandle.class);
    
    @SuppressWarnings("static-access")
    public void commitJob(Thread thread)
    {
        while (true)
        {
            boolean result = JobManager.getInstance().submitJob(thread);
            if (result)
            {
                break;
            }
            else
            {
                try
                {
                    thread.sleep(1000);
                }
                catch (InterruptedException e)
                {
                    log.error(e.getMessage(), e);
                    break;
                }
            }
        }
    }
}
