package com.teamface.common.util.activiti;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.ProcessEngine;
import org.activiti.spring.SpringProcessEngineConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import com.alibaba.druid.pool.DruidDataSource;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.SpringContextUtil;

/**
 * @Description: Activiti流程服务，提供流程引擎
 * @author: ZZH
 * @date: 2017年12月19日 上午11:02:03
 * @version: 1.0
 */

public class ActivitiServer
{
    final Logger log = Logger.getLogger(ActivitiServer.class);
    
    private static ActivitiServer instance = null;
    
    private String databaseTablePrefix = null;
    
    private ProcessEngine processEngine = null;
    
    private static String lastDatabaseTablePrefix = null;
    
    private Map<String, Long> cachedUsedTimes = new HashMap<>();
    
    private Map<String, ProcessEngine> cachedProcessEngines = new HashMap<>();
    
    public static synchronized ActivitiServer getInstance(String databaseTablePrefix)
    {
        if (lastDatabaseTablePrefix == null)
        {
            lastDatabaseTablePrefix = databaseTablePrefix;
        }
        else if (!lastDatabaseTablePrefix.equals(databaseTablePrefix))
        {
            instance = null;
            lastDatabaseTablePrefix = databaseTablePrefix;
        }
        if (instance == null)
        {
            instance = new ActivitiServer(databaseTablePrefix);
        }
        
        return instance;
    }
    
    public ProcessEngine getProcessEngine()
    {
        return processEngine;
    }
    
    public void closeProcessEngine()
    {
        try
        {
            processEngine.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        try
        {
            cachedUsedTimes.remove(databaseTablePrefix);
            cachedProcessEngines.remove(databaseTablePrefix);
            initProcessEngine();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
    }
    
    private void close(String key)
    {
        try
        {
            cachedProcessEngines.get(key).close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        try
        {
            cachedUsedTimes.remove(key);
            cachedProcessEngines.remove(key);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private ActivitiServer(String databaseTablePrefix)
    {
        this.databaseTablePrefix = databaseTablePrefix;
        
        log.debug("databaseTablePrefix:" + databaseTablePrefix + ",cachedProcessEngines.size:" + cachedProcessEngines.size());
        processEngine = cachedProcessEngines.get(databaseTablePrefix);
        if (processEngine == null)
        {
            initProcessEngine();
        }
        else
        {
            Long time = System.currentTimeMillis();
            if (cachedProcessEngines.size() >= Constant.PROCESS_MAX_CACHED_SIZE)
            {
                cachedUsedTimes.remove(databaseTablePrefix);
                String key = getPrefix4MinTime();
                if (key != null)
                {
                    close(key);
                }
            }
            cachedUsedTimes.put(databaseTablePrefix, time);
        }
    }
    
    private void initProcessEngine()
    {
        try
        {
            DruidDataSource dataSource = (DruidDataSource)SpringContextUtil.getBean("dataSource");
            DataSourceTransactionManager transactionManager = (DataSourceTransactionManager)SpringContextUtil.getBean("transactionManager");
            Map<Object, Object> beans = new HashMap<>();
            beans.put("assigneeServer", new AssigneeServer());
            SpringProcessEngineConfiguration configuration = new SpringProcessEngineConfiguration();
            configuration.setTransactionManager(transactionManager);
            configuration.setBeans(beans);
            configuration.setDataSource(dataSource);
            if (!StringUtils.isEmpty(databaseTablePrefix))
            {
                configuration.setDatabaseTablePrefix(databaseTablePrefix);
            }
            configuration.setDatabaseSchemaUpdate("false").setJobExecutorActivate(false).setDbIdentityUsed(false);
            processEngine = configuration.getProcessEngineConfiguration().buildProcessEngine();
            cachedUsedTimes.put(databaseTablePrefix, 1l);
            cachedProcessEngines.put(databaseTablePrefix, processEngine);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private String getPrefix4MinTime()
    {
        Long min = System.currentTimeMillis();
        Map<Long, String> timesMap = new HashMap<>();
        for (Map.Entry<String, Long> entry : cachedUsedTimes.entrySet())
        {
            String prefix = entry.getKey();
            Long time = entry.getValue();
            log.debug("databaseTablePrefix:" + prefix + ",time:" + new Date(time).toString());
            if (time < min)
            {
                min = time;
                timesMap.put(time, prefix);
            }
        }
        
        return timesMap.get(min);
    }
}
