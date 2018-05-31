package com.teamface.common.util.dao;

import java.sql.Connection;
import java.sql.SQLException;

import org.apache.log4j.Logger;

import com.alibaba.druid.pool.DruidDataSource;
import com.teamface.common.util.SpringContextUtil;

/**
 * @Description:
 * @author: zhangzhihua
 * @date: 2017年9月18日 上午10:31:39
 * @version: 1.0
 */

public class DBConnectionPool
{
    
    final Logger log = Logger.getLogger(DBConnectionPool.class);
    
    private static DBConnectionPool instance = null;
    
    private DruidDataSource druidDataSource = null;
    
    public static synchronized DBConnectionPool getInstance()
    {
        if (instance == null)
        {
            instance = new DBConnectionPool();
        }
        
        return instance;
    }
    
    public DBConnectionPool()
    {
        try
        {
            druidDataSource = (DruidDataSource)SpringContextUtil.getBean("dataSource");
        }
        catch (Exception e)
        {
            log.error(e.toString());
        }
    }
    
    public Connection getConnection()
    {
        try
        {
            return druidDataSource.getConnection();
        }
        catch (SQLException e)
        {
            log.error(e.toString());
            return null;
        }
    }
    
    public void closeConnection(Connection conn)
    {
        try
        {
            if (conn != null)
            {
                conn.close();
            }
        }
        catch (SQLException e)
        {
            log.error(e.toString());
        }
    }
}
