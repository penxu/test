package com.teamface.custom.async;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @Description:
 * @author: Administrator
 * @date: 2018年4月18日 上午9:56:17
 * @version: 1.0
 */

public class ExecutorThreadPool
{
    private static ExecutorService executorService = null;
    
    private ExecutorThreadPool()
    {
        
    }
    
    public static ExecutorService getInstance()
    {
        if (executorService == null)
        {
            executorService = Executors.newCachedThreadPool();
        }
        return executorService;
    }
}
