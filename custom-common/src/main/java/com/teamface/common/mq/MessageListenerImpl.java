package com.teamface.common.mq;

import java.io.IOException;

/**
 * 具体的监听器
 * 
 * @author caojianhua
 *        
 */
public class MessageListenerImpl implements MessageListener
{
    
    @Override
    public void onMessage(String message)
        throws IOException
    {
        System.out.println("监听到的消息：" + message);
    }
    
}
