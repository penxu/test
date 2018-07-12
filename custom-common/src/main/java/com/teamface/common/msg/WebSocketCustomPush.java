package com.teamface.common.msg;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URISyntaxException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.teamface.common.constant.Constant;
import com.teamface.common.msg.constant.MsgConstant;
import com.teamface.common.msg.pojo.LoginRequestPojo;
import com.teamface.common.msg.pojo.PushPojo;
import com.teamface.common.msg.util.WebsocketDataProcess;

/**
 * @Description:
 * @author: dsl
 * @date: 2017年11月14日 下午5:42:23
 * @version: 1.0
 */

public class WebSocketCustomPush
{
    
    private static Logger log = LogManager.getLogger(WebSocketCustomPush.class);
    
    private static boolean isLogin = false;
    
    private static WebSocketClientServer webSocketClient;
    
    private static WebSocketCustomPush webSocketCustomPush;
    
    public static WebSocketCustomPush getInstance()
    {
        webSocketClient = WebSocketClientServer.getInstance();
        if (null == webSocketCustomPush)
        {
            webSocketCustomPush = new WebSocketCustomPush();
        }
        return webSocketCustomPush;
    }
    
    /**
     * 
     * @throws URISyntaxException
     * @throws SocketTimeoutException
     * @throws SocketException
     * @throws IOException
     * @Description: 登录
     */
    public boolean login(String sender, long senderId, long receiverId, int ucDeviceType)
    {
        try
        {
            if (isLogin)
            {
                return true;
            }
            // 命令包头
            byte[] cmdHead = WebsocketDataProcess.setCmdHead(Constant.PUSH_ACCOUNT, MsgConstant.IM_LOG_IN_CMD, Constant.PUSH_ACCOUNT, receiverId, 1);
            // 登录
            LoginRequestPojo loginRequestPojo = new LoginRequestPojo();
            loginRequestPojo.setSzUsername(String.valueOf(Constant.PUSH_ACCOUNT));
            loginRequestPojo.setChStatus((byte)1);
            loginRequestPojo.setChUserType((byte)1);
            byte[] loginReq = loginRequestPojo.toByte();
            // 请求数据
            byte[] reqData = WebsocketDataProcess.mergeRequestInfo(cmdHead, loginReq);
            boolean flag = false;
            while (!webSocketClient.getReadyState().name().equals("OPEN"))
            {
                log.warn("Websocket is not connecting ......");
                Thread.sleep(3000);
                if (!flag)
                {
                    flag = true;
                }
                else
                {
                    return false;
                }
                
            }
            webSocketClient.pushMessage(reqData);
        }
        catch (InterruptedException e)
        {
            log.error(e.getMessage(), e);
        }
        isLogin = true;
        return true;
        
    }
    
    /**
     * 
     * @param content 推送数据
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param ucDeviceType
     * @return
     * @Description:发送单条推送
     */
    public synchronized boolean singlePush(String content, long senderId, long receiverId, int ucDeviceType)
    {
        if (!webSocketClient.getReadyState().name().equals("OPEN"))
        {
            webSocketClient = webSocketClient.clientConnect();
            isLogin = false;
        }
        if (!isLogin)
        {
            boolean loginStatus = login(String.valueOf(Constant.PUSH_ACCOUNT), senderId, receiverId, ucDeviceType);
            if (!loginStatus)
            {
                return false;
            }
        }
        // 命令包头
        byte[] cmdHead = WebsocketDataProcess.setCmdHead(Constant.PUSH_ACCOUNT, MsgConstant.IM_USER_DEFINED_PERSONAL_CMD, Constant.PUSH_ACCOUNT, receiverId, 1);
        // 推送内容
        PushPojo pushPojo = new PushPojo();
        pushPojo.setMessage(content);
        // 请求数据
        byte[] reqData = null;
        try
        {
            reqData = WebsocketDataProcess.mergeRequestInfo(cmdHead, pushPojo.getMessage().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e1)
        {
            log.error(e1.getMessage(), e1);
        }
        boolean flag = false;
        while (!webSocketClient.getReadyState().name().equals("OPEN"))
        {
            log.warn("Websocket is not connecting ......");
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                log.error(e.getMessage(), e);
            }
            if (!flag)
            {
                flag = true;
            }
            else
            {
                return false;
            }
        }
        log.warn("推送数据：" + pushPojo.getMessage());
        // 发送请求
        webSocketClient.pushMessage(reqData);
        // 返回执行结果
        return true;
    }
    
    /**
     * 
     * @param content 群推送内容
     * @param senderId 发送者ID
     * @param receiverId 接收者ID
     * @param ucDeviceType
     * @return
     * @Description:推送群数据
     */
    public synchronized boolean groupPush(String content, long senderId, long receiverId, int ucDeviceType)
    {
        if (!webSocketClient.getReadyState().name().equals("OPEN"))
        {
            webSocketClient = webSocketClient.clientConnect();
            isLogin = false;
        }
        if (!isLogin)
        {
            boolean loginStatus = login(String.valueOf(Constant.PUSH_ACCOUNT), senderId, receiverId, ucDeviceType);
            if (!loginStatus)
            {
                return false;
            }
        }
        // 命令包头
        byte[] cmdHead = WebsocketDataProcess.setCmdHead(Constant.PUSH_ACCOUNT, MsgConstant.IM_USER_DEFINED_TEAM_CMD, Constant.PUSH_ACCOUNT, receiverId, 1);
        // 推送内容
        PushPojo pushPojo = new PushPojo();
        pushPojo.setMessage(content);
        // 请求数据
        byte[] reqData = null;
        try
        {
            reqData = WebsocketDataProcess.mergeRequestInfo(cmdHead, pushPojo.getMessage().getBytes("UTF-8"));
        }
        catch (UnsupportedEncodingException e1)
        {
            log.error(e1.getMessage(), e1);
        }
        boolean flag = false;
        while (!webSocketClient.getReadyState().name().equals("OPEN"))
        {
            log.warn("Websocket is not connecting ......");
            try
            {
                Thread.sleep(1000);
            }
            catch (InterruptedException e)
            {
                log.error(e.getMessage(), e);
            }
            if (!flag)
            {
                flag = true;
            }
            else
            {
                return false;
            }
            
        }
        log.warn("推送数据：" + pushPojo.getMessage());
        // 发送请求
        webSocketClient.pushMessage(reqData);
        // 返回执行结果
        return true;
    }
    
}
