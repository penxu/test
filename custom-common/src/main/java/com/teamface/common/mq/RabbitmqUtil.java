package com.teamface.common.mq;

import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

/**
 * 测试程序（提供连接）
 * 
 * @author caojianhua
 *         
 */
public class RabbitmqUtil
{
    static String host = "192.168.1.220";
    
    static String username = "hjhq";
    
    static String password = "123456";
    
    public static Connection getConnection()
        throws Exception
    {
        Connection connection = null;
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost(host);
        factory.setUsername(username);
        factory.setPassword(password);
        connection = factory.newConnection();
        return connection;
    }
}
