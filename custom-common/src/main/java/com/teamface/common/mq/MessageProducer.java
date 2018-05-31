package com.teamface.common.mq;

import java.util.Scanner;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.MessageProperties;

/**
 * 测试程序（RibbitMQ发送端）
 * 
 * @author caojianhua
 *        
 */
public class MessageProducer
{
    private static final String TASK_QUEUE_NAME = "QUEUE_CRM";
    
    // private static final String TASK_EXCHANGE_NAME = "EXCHANGE_DIRECT_CRM";
    public static Connection connection;
    
    public static Channel channel;
    
    @SuppressWarnings("resource")
    public static void main(String[] argv)
        throws Exception
    {
        initRabbitMQ();
        String msg = "";
        boolean run = true;
        do
        {
            System.out.println("请输入发送消息模式(1:发送单条, 2:发送批量, quit:退出)：");
            Scanner sc = new Scanner(System.in);
            msg = sc.next();
            if ("quit".equals(msg))
            {
                run = false;
            }
            else if ("1".equals(msg))
            {// 发送单个消息到queue
                System.out.println("请输入要发送消息内容：");
                Scanner sn = new Scanner(System.in);
                String single = sn.next();
                msgProducerSingle("QUEUE_CRM", single);
            }
            else if ("2".equals(msg))
            {// 发送批量消息到queue
                String message = "CRM测试消息";
                msgProducerBatch("QUEUE_CRM", message);
            }
            else
            {
            
            }
        } while (run);
        destroyRabbitMQ();
    }
    
    /** 初始化MQ */
    public static void initRabbitMQ()
    {
        try
        {
            connection = RabbitmqUtil.getConnection();
            channel = connection.createChannel();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /** 销毁MQ */
    public static void destroyRabbitMQ()
    {
        try
        {
            if (null != connection)
            {
                connection.close();
                connection = null;
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
    
    /** 发送单条信息 */
    public static boolean msgProducerSingle(String queueName, String message)
    {
        boolean result = false;
        try
        {
            channel.queueDeclare(queueName, true, false, false, null);
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
            System.out.println("[RabbitMQ] Send '" + message + "'");
            result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
    
    /** 发送批量信息 */
    public static boolean msgProducerBatch(String queueName, String message)
    {
        boolean result = false;
        try
        {
            Connection connection = RabbitmqUtil.getConnection();
            Channel channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            for (int i = 0; i < 10; i++)
            {
                channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, (message + i).getBytes("UTF-8"));
                System.out.println("Sent Message:" + message + i);
            }
            result = true;
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return result;
    }
}
