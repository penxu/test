package com.teamface.common.mq;

import java.io.IOException;

/**
 * 消息队列监听器，所有监听消费者都要实现此类
 * 
 * @author caojianhua
 *         
 */
public interface MessageListener
{
    void onMessage(String message)
        throws IOException;
}
