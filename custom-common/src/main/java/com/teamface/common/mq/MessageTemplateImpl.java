package com.teamface.common.mq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 消息模版实现类
 * 
 * @author caojianhua
 *         
 */
public class MessageTemplateImpl implements MessageTemplate
{
    private static Logger log = Logger.getLogger(MessageTemplateImpl.class);
    
    private Connection connection;
    
    private Channel channel;
    
    public MessageTemplateImpl(Connection connection, Channel channel)
    {
        this.connection = connection;
        this.channel = channel;
    }
    
    @Override
    public boolean send(String queueName, String message)
        throws IOException
    {
        boolean result = false;
        channel.queueDeclare(queueName, true, false, false, null);
        channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes());
        log.info("Send single Message :" + message);
        result = true;
        return result;
    }
    
    @Override
    public boolean send(String queueName, List<String> messageList)
        throws UnsupportedEncodingException, IOException
    {
        boolean result = false;
        channel.queueDeclare(queueName, true, false, false, null);
        for (String message : messageList)
        {
            channel.basicPublish("", queueName, MessageProperties.PERSISTENT_TEXT_PLAIN, message.getBytes("UTF-8"));
            log.info("Send batch message :" + message);
        }
        result = true;
        return result;
    }
    
    @Override
    public List<String> receive(String queueName)
        throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException
    {
        List<String> result = new ArrayList<String>();
        channel.queueDeclare(queueName, true, false, false, null);
        while (true)
        {
            GetResponse resp = channel.basicGet(queueName, true);
            if (resp == null)
            {
                log.error("There's no more news!");
                break;
            }
            else
            {
                String message = new String(resp.getBody(), "UTF-8");
                result.add(message);
                log.info("Receive batch message : " + message);
            }
        }
        return result;
    }
    
    @Override
    public List<String> receive(String queueName, int size)
        throws IOException
    {
        List<String> result = new ArrayList<String>();
        channel.queueDeclare(queueName, true, false, false, null);
        while (true)
        {
            if (size == 0)
                break;
            GetResponse resp = channel.basicGet(queueName, true);
            if (resp == null)
            {
                log.error("There's no more news!");
                break;
            }
            else
            {
                String message = new String(resp.getBody(), "UTF-8");
                result.add(message);
                log.info("Receive batch message : " + message);
            }
            size--;
        }
        return result;
    }
    
}
