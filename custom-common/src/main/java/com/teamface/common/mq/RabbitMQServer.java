package com.teamface.common.mq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.Logger;

import com.rabbitmq.client.AMQP.Queue.DeclareOk;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.GetResponse;
import com.rabbitmq.client.MessageProperties;
import com.teamface.common.util.PropertiesConfigObject;

/**
 * @Description:
 * @author: ZZH
 * @date: 2017年11月30日 上午10:32:02
 * @version: 1.0
 */

public class RabbitMQServer
{
    
    private final static Logger log = Logger.getLogger(RabbitMQServer.class);
    
    // 获取配置文件实例
    private static final PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
    
    // 获取安全配置对象
    private static final Configuration config = properties.getConfig();
    
    private Connection connection = null;
    
    private static Boolean init = false;
    
    public RabbitMQServer()
    {
        synchronized (init)
        {
            if (!init)
            {
                getConnection();
                init = true;
            }
        }
    }
    
    private void getConnection()
    {
        try
        {
            try
            {
            	if(connection!=null)
            	{
            		connection.close();
                    connection = null;
            	}
            }
            catch (Exception e)
            {
            }
            String host = config.getString("rabbitmq.host");
            int port = config.getInt("rabbitmq.port");
            String username = config.getString("rabbitmq.username");
            String password = config.getString("rabbitmq.password");
            ConnectionFactory factory = new ConnectionFactory();
            factory.setUsername(username);
            factory.setPassword(password);
            factory.setHost(host);
            factory.setPort(port);
            
            // 指定线程池
            ExecutorService service = Executors.newFixedThreadPool(10);
            factory.setSharedExecutor(service);
            // 设置自动恢复
            factory.setAutomaticRecoveryEnabled(true);
            // 设置 没10s ，重试一次
            factory.setNetworkRecoveryInterval(5000);
            // 设置不重新声明交换器，队列等信息。
            factory.setTopologyRecoveryEnabled(false);

            
            connection = factory.newConnection();
            init = true;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    public void sendMessage(String queue, String object)
    {
        Channel channel = getChannel(queue);
        try
        {
            if (channel != null)
            {
                byte[] json = object.getBytes("utf-8");
                channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, json);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(channel);
        }
    }
    
    public void sendMessages(String queue, List<String> list)
    {
        if (null == list || list.size() == 0)
        {
            return;
        }
        long start = System.currentTimeMillis();
        Channel channel = getChannel(queue);
        try
        {
            if (channel != null)
            {
                for (String object : list)
                {
                    byte[] json = object.getBytes("utf-8");
                    
                    channel.basicPublish("", queue, MessageProperties.PERSISTENT_TEXT_PLAIN, json);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(channel);
            double cost = (System.currentTimeMillis() - start) / 1000.0;
            log.debug("sendMessages=>cost:" + cost + " sec.");
        }
        
    }
    
    /**
     * Get messages of specified number,if that queue is empty,then it will
     * waiting until timeout(default:10 secs).
     * 
     * @param queueName
     * @param count
     * @return
     */
    public List<String> getMessagesNonblocking(String queueName, int count)
    {
        int i = 0;
        List<String> list = new ArrayList<>();
        Channel channel = getChannel(queueName);
        try
        {
            long start = System.currentTimeMillis();
            while (i < count)
            {
                GetResponse response = channel.basicGet(queueName, false);
                if (response != null)
                {
                    String msg = new String(response.getBody(), "utf-8");
                    list.add(msg);
                    log.debug("Message:" + msg);
                    channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
                    i++;
                    continue;
                }
                else
                {
                    Thread.sleep(1000);
                }
                // timeout
                if ((System.currentTimeMillis() - start) / 1000 > 10)
                {
                    break;
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(channel);
        }
        return list;
    }
    
    /**
     * Get a message from message queue.
     * 
     * @param queueName
     * @param count
     * @return
     */
    public String getMessageNonblocking(String queueName)
    {
        Channel channel = getChannel(queueName);
        String msg = null;
        try
        {
            GetResponse response = channel.basicGet(queueName, false);
            if (response != null)
            {
                msg = new String(response.getBody(), "utf-8");
                log.debug("Message:" + msg);
                channel.basicAck(response.getEnvelope().getDeliveryTag(), false);
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            close(channel);
        }
        return msg;
    }
    
    public int getCountOfQueue(String queueName)
    {
        Channel channel = null;
        if (connection == null)
        {
            getConnection();
        }
        synchronized (connection)
        {
            try
            {
                if (!connection.isOpen())
                {
                    getConnection();
                }
                channel = connection.createChannel();
                DeclareOk declareOk = channel.queueDeclare(queueName, true, false, false, null);
                return declareOk.getMessageCount();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
                return 0;
            }
            finally
            {
                close(channel);
            }
        }
        
    }
    
    public void deleteQueue(String queueName)
    {
        Channel channel = null;
        if (connection == null)
        {
            getConnection();
        }
        synchronized (connection)
        {
            try
            {
                if (!connection.isOpen())
                {
                    getConnection();
                }
                channel = connection.createChannel();
                channel.queueDelete(queueName);
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
            finally
            {
                close(channel);
            }
        }
    }
    
    /**
     * Get a channel and declare queue.
     * 
     * @param exchannelName
     * @return
     */
    private Channel getChannel(String queueName)
    {
        Channel channel = null;
        try
        {
            if (connection == null)
            {
                getConnection();
            }
            synchronized (connection)
            {
                
                if (!connection.isOpen())
                {
                    getConnection();
                }
            }
            channel = connection.createChannel();
            channel.queueDeclare(queueName, true, false, false, null);
            return channel;
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
    }
    
    public void close(Channel channel)
    {
        try
        {
            if (channel != null)
            {
                channel.close();
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
    }
    
}