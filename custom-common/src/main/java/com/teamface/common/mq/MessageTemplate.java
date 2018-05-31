package com.teamface.common.mq;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.ShutdownSignalException;

/**
 * 发送、接收消息模版
 * 
 * @author caojianhua
 *         
 */
public interface MessageTemplate
{
    boolean send(String queueName, String message)
        throws IOException;
        
    boolean send(String queueName, List<String> messageList)
        throws UnsupportedEncodingException, IOException;
        
    List<String> receive(String queueName)
        throws IOException, ShutdownSignalException, ConsumerCancelledException, InterruptedException;
        
    List<String> receive(String queueName, int size)
        throws IOException;
}
