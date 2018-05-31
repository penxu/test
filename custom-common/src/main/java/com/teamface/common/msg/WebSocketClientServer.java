package com.teamface.common.msg;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.ByteBuffer;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import javax.security.cert.X509Certificate;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import com.teamface.common.constant.Constant;
import com.teamface.common.msg.constant.MsgConstant;
import com.teamface.common.msg.pojo.CmdHeadPojo;
import com.teamface.common.msg.pojo.LoginRequestPojo;
import com.teamface.common.msg.pojo.MsgPojo;
import com.teamface.common.msg.util.WebsocketDataProcess;
import com.teamface.common.util.PropertiesConfigObject;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年11月10日 下午3:56:32
 * @version: 1.0
 */

public class WebSocketClientServer extends WebSocketClient
{
    private static final Logger log = LogManager.getLogger(WebSocketClientServer.class);
    
    private static WebSocketClientServer webSocketClientServer;
    
    // 消息服务器websocket地址
    private static String uri;
    
    private boolean receiveStatus = false;
    
    private byte[] byteMessage;
    
    private static int executeTimes;
    
    private static boolean linkRetry = false;
    
    private static int connectRetry;
    
    private static SSLContext sslContext = null;
    
    private static SSLSocketFactory factory;
    
    private static int connectTunnel;
    
    private static boolean retryStatus = true;
    
    static
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        connectTunnel = config.getInt("websocket.tunnel");
        executeTimes = config.getInt("websocket.send.retry");
        connectRetry = config.getInt("websocket.connect.retry");
        uri = connectTunnel == 0 ? uri = config.getString("websocket.ws.link") : config.getString("websocket.wss.link");
        if (connectTunnel == 1)
        {
            try
            {
                sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, new TrustManager[] {new X509TrustManager()
                {
                    @SuppressWarnings("unused")
                    public void checkClientTrusted(X509Certificate[] certs, String authType)
                    {
                        System.out.println("checkClientTrusted1");
                    }
                    
                    @Override
                    public void checkClientTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException
                    {
                        System.out.println("checkClientTrusted2");
                    }
                    
                    @SuppressWarnings("unused")
                    public void checkServerTrusted(X509Certificate[] certs, String authType)
                    {
                        System.out.println("checkServerTrusted1");
                    }
                    
                    @Override
                    public void checkServerTrusted(java.security.cert.X509Certificate[] arg0, String arg1)
                        throws CertificateException
                    {
                        System.out.println("checkServerTrusted2");
                    }
                    
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers()
                    {
                        return null;
                    }
                }}, new SecureRandom());
                factory = sslContext.getSocketFactory();
            }
            catch (NoSuchAlgorithmException e)
            {
                log.error(e.getMessage(), e);
            }
            catch (KeyManagementException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        
    }
    
    public static WebSocketClientServer getInstance()
    {
        if (null == webSocketClientServer)
        {
            try
            {
                webSocketClientServer = new WebSocketClientServer(new URI(uri));
                if (connectTunnel == 1)
                {
                    webSocketClientServer.setSocket(factory.createSocket());
                }
            }
            catch (URISyntaxException e)
            {
                log.error(e.getMessage(), e);
                
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
        }
        return webSocketClientServer;
    }
    
    public WebSocketClientServer(URI serverUri)
    {
        super(serverUri);
        
    }
    
    public boolean pushMessage(byte[] data)
    {
        log.debug("pushMessage,start !");
        byteMessage = data;
        do
        {
            send(data);
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                log.error(e.getMessage(), e);
            }
            log.debug(String.format("pushMessage,confirm!"));
            System.out.println(String.format("pushMessage,confirm!"));
            executeTimes--;
        } while (!receiveStatus && executeTimes > 0);
        executeTimes = 0;
        log.debug("pushMessage, end !");
        return true;
    }
    
    public WebSocketClientServer clientConnect()
    {
        log.debug("pushMessage,start !");
        try
        {
            webSocketClientServer.connect();
            return webSocketClientServer;
        }
        catch (IllegalStateException e)
        {
            log.error(e.getMessage(), e);
            connectRetry();
            return webSocketClientServer;
        }
    }
    
    @Override
    public void onClose(int arg0, String arg1, boolean arg2)
    {
        log.warn("websocket closed...");
        if (retryStatus)
        {
            connectRetry();
        }
        else
        {
            try
            {
                webSocketClientServer = new WebSocketClientServer(new URI(uri));
            }
            catch (URISyntaxException e)
            {
                log.error(e.getMessage(), e);
            }
        }
    }
    
    @Override
    public void onError(Exception arg0)
    {
        arg0.printStackTrace();
        log.warn("websocket error...");
    }
    
    @Override
    public void onMessage(String arg0)
    {
        log.info("websocket received info:" + arg0);
    }
    
    @Override
    public void onMessage(ByteBuffer bytes)
    {
        log.debug(String.format("receiveMessage,start!"));
        if (!linkRetry)
        {
            CmdHeadPojo sendContent = WebsocketDataProcess.getCmdHeadInfo(byteMessage);
            MsgPojo sendMsgPojo = sendContent.getMsgPojo();
            byte[] byteArr = new byte[bytes.remaining()];
            bytes.get(byteArr, 0, byteArr.length);
            CmdHeadPojo receiveContent = WebsocketDataProcess.getCmdHeadInfo(byteArr);
            MsgPojo receiveMsgPojo = receiveContent.getMsgPojo();
            if (sendMsgPojo.getSenderID() == receiveMsgPojo.getSenderID() && sendMsgPojo.getSenderID() == receiveMsgPojo.getSenderID()
                && sendMsgPojo.getClientTimes() == receiveMsgPojo.getClientTimes() && sendMsgPojo.getRand() == receiveMsgPojo.getRand())
            {
                receiveStatus = true;
            }
        }
        else
            linkRetry = false;
        log.debug("receiveMessage, end !");
    }
    
    @Override
    public void onOpen(ServerHandshake arg0)
    {
        log.warn("websocket opened...");
    }
    
    private void connectRetry()
    {
        try
        {
            linkRetry = true;
            webSocketClientServer = new WebSocketClientServer(new URI(uri));
            if (connectTunnel == 1)
            {
                webSocketClientServer.setSocket(factory.createSocket());
            }
            webSocketClientServer.connect();
            byte[] cmdHead = WebsocketDataProcess.setCmdHead(Constant.PUSH_ACCOUNT, MsgConstant.IM_LOG_IN_CMD, Constant.PUSH_ACCOUNT, Constant.PUSH_ACCOUNT, 1);
            // 登录
            LoginRequestPojo loginRequestPojo = new LoginRequestPojo();
            loginRequestPojo.setSzUsername(String.valueOf(Constant.PUSH_ACCOUNT));
            loginRequestPojo.setChStatus((byte)1);
            loginRequestPojo.setChUserType((byte)1);
            byte[] loginReq = loginRequestPojo.toByte();
            // 请求数据
            byte[] reqData = WebsocketDataProcess.mergeRequestInfo(cmdHead, loginReq);
            while (!webSocketClientServer.getReadyState().name().equals("OPEN"))
            {
                log.warn("Websocket is not connecting ......");
                Thread.sleep(3000);
                if (connectRetry > 0)
                {
                    connectRetry--;
                }
                else
                {
                    retryStatus = false;
                    return;
                }
            }
            webSocketClientServer.send(reqData);
        }
        catch (URISyntaxException e)
        {
            log.error(e.getMessage(), e);
            retryStatus = false;
        }
        catch (InterruptedException e)
        {
            log.error(e.getMessage(), e);
            retryStatus = false;
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
            retryStatus = false;
        }
    }
}
