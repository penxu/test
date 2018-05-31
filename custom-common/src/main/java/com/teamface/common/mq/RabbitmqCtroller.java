package com.teamface.common.mq;

import java.io.IOException;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.configuration2.Configuration;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.teamface.common.util.PropertiesConfigObject;

/**
 * rabbitmq控制器
 * 
 * @author Administrator
 *         
 */
public class RabbitmqCtroller
{
    private MessageTemplate messageTemplate;
    
    private Connection connection;
    
    private Channel channel;
    
    private AtomicBoolean isConnection = new AtomicBoolean(false);
    
    private static RabbitmqCtroller rabbitmqCtroller;
    
    public synchronized static RabbitmqCtroller getInstance()
    {
        if (rabbitmqCtroller == null)
        {
            rabbitmqCtroller = new RabbitmqCtroller();
        }
        return rabbitmqCtroller;
    }
    
    private RabbitmqCtroller()
    {
        initRabbitmqConnectionFactory();
        setMessageTemplate(new MessageTemplateImpl(connection, channel));
        new MessageAdapterHandler(connection, channel);
    }
    
    private void initRabbitmqConnectionFactory()
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost(config.getString("rabbitmq.host"));
        // connectionFactory.setPort(Integer.valueOf(configProCache.get("rabbitmq.port")));
        connectionFactory.setUsername(config.getString("rabbitmq.username"));
        connectionFactory.setPassword(config.getString("rabbitmq.password"));
        try
        {
            connection = connectionFactory.newConnection();
            channel = connection.createChannel();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (TimeoutException e)
        {
            e.printStackTrace();
        }
        isConnection.set(true);
    }
    
    /**
     * 注销程序
     */
    public synchronized void destroy()
        throws Exception
    {
        if (!isConnection.get())
        {
            return;
        }
        setMessageTemplate(null);
        connection.close();
        channel.close();
    }
    
    public MessageTemplate getMessageTemplate()
    {
        return messageTemplate;
    }
    
    public void setMessageTemplate(MessageTemplate messageTemplate)
    {
        this.messageTemplate = messageTemplate;
    }
}
