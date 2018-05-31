package com.teamface.common.mq;

import java.io.IOException;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.Envelope;
import com.teamface.common.util.PropertiesConfigObject;

/**
 * 消息适配器
 * 
 * @author caojianhua
 *         
 */
public class MessageAdapterHandler
{
    private static final Logger logger = Logger.getLogger(MessageAdapterHandler.class);
    
    private static final PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
    
    private static final Configuration config = properties.getConfig();
    
    private static final String DEFUALT_QUEUE_NAME = config.getString("rabbitmq.default_queue");
    
    private MessageListener messageListener;
    
    private Connection connection;
    
    private Channel channel;
    
    public MessageAdapterHandler(Connection connection, Channel channel)
    {
        this.connection = connection;
        this.channel = channel;
        messageListener = new MessageListenerImpl();
        handleMessage();
    }
    
    public void handleMessage()
    {
        try
        {
            // 声明要关注的队列
            channel.queueDeclare(DEFUALT_QUEUE_NAME, true, false, false, null);
            System.out.println("Customer Waiting Received messages");
            
            // 告诉服务器我们需要那个频道的消息，如果频道中有消息，就会执行回调函数handleDelivery
            Consumer consumer = new DefaultConsumer(channel)
            {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException
                {
                    String message = new String(body, "UTF-8");
                    messageListener.onMessage(message);
                    System.out.println("Customer Received '" + message + "'");
                }
            };
            // 自动回复队列应答 -- RabbitMQ中的消息确认机制
            channel.basicConsume(DEFUALT_QUEUE_NAME, true, consumer);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}
