package com.teamface.common.msg;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.teamface.common.msg.constant.MsgConstant;
import com.teamface.common.msg.pojo.CmdHeadPojo;
import com.teamface.common.msg.pojo.MsgPojo;
import com.teamface.common.msg.pojo.OuterHeadPojo;
import com.teamface.common.msg.util.WebsocketDataProcess;
import com.teamface.common.util.dao.JedisClusterHelper;

/**
 * @Description:
 * @author: Administrator
 * @date: 2017年10月11日 下午2:39:00
 * @version: 1.0
 */
public class TcpClientSocket
{
    private static final Logger log = LogManager.getLogger(TcpClientSocket.class);
    
    private Socket socket = null;
    
    private OutputStream os = null;
    
    private InputStream is = null;
    
    private boolean isConnection = false;
    
    private static TcpClientSocket clientSocket;
    
    private static final int HEADLENGTH = 8;
    
    /** 推送服务器ip */
    private static String pushServerIp;
    
    /** 推送服务器端口 */
    private static int pushServerPort;
    
    /**
     * 构造函数，创建Tcp客户端
     */
    public static TcpClientSocket getInstance()
    {
        if (null == clientSocket)
        {
            clientSocket = new TcpClientSocket();
        }
        return clientSocket;
    }
    
    private TcpClientSocket()
    {
        try
        {
            startConnection();
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * @return
     * @throws SocketException
     * @throws SocketTimeoutException
     * @throws IOException
     * @Description: 打开连接
     */
    public boolean startConnection()
        throws IOException
    {
        if (isConnection)
        {
            return true;
        }
        
        pushServerIp = "192.168.1.168"; // config.getString("push.server.ip");
        pushServerPort = 8851; // config.getInt("push.server.port");
        
        socket = new Socket();
        socket.connect(new InetSocketAddress(pushServerIp, pushServerPort), 10000);
        // 设置 socket 读取数据流的超时时间
        if (socket == null)
        {
            return false;
        }
        
        socket.setSoTimeout(0);
        // 发送数据包，默认为 false，即客户端发送数据采用 Nagle 算法；
        // 但是对于实时交互性高的程序，建议其改为 true，即关闭 Nagle
        // 算法，客户端每发送一次数据，无论数据包大小都会将这些数据发送出去
        socket.setTcpNoDelay(true);
        // 设置客户端 socket 关闭时，close() 方法起作用时延迟 30 秒关闭，如果 30 秒内尽量将未发送的数据包发送出去
        socket.setSoLinger(true, 30);
        // 设置输出流的发送缓冲区大小，默认是4KB，即4096字节
        socket.setSendBufferSize(2048);
        // 设置输入流的接收缓冲区大小，默认是4KB，即4096字节
        socket.setReceiveBufferSize(2048);
        socket.setReuseAddress(true);
        
        if (socket.isConnected())
        {
            isConnection = true;
            os = socket.getOutputStream();
            is = socket.getInputStream();
            return true;
        }
        else
        {
            isConnection = false;
            return false;
        }
        
    }
    
    /**
     * 设置超时时间，该方法必须在bind方法之后使用.
     * 
     * @param timeout 超时时间
     * @throws Exception
     */
    public void setSoTimeout(final int timeout)
    {
        try
        {
            socket.setSoTimeout(timeout);
        }
        catch (SocketException e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    /**
     * 获得超时时间.
     * 
     * @return 返回超时时间
     * @throws Exception
     */
    public int getSoTimeout()
        throws Exception
    {
        return socket.getSoTimeout();
    }
    
    public final Socket getSocket()
    {
        return socket;
    }
    
    public boolean isAlive()
    {
        return (null != this.socket && this.socket.isConnected());
    }
    
    public boolean isConnection()
    {
        try
        {
            socket.sendUrgentData(0xFF);
            return true;
        }
        catch (IOException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * 关闭tcp连接.
     */
    public final void close()
    {
        try
        {
            if (socket != null)
            {
                socket.close();
                socket = null;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            isConnection = false;
        }
    }
    
    /**
     * 向指定的服务端发送数据信息
     * 
     * @param host 服务器主机地地址
     * @param port 服务端端口
     * @param bytes 发送的数据信息
     * @throws IOException
     */
    public synchronized final void send(byte[] bytes)
        throws IOException
    {
        if (os != null)
        {
            os.write(bytes);
            os.flush();
        }
        else
        {
            throw new IOException();
        }
    }
    
    /**
     * 接收从指定的服务端发回的数据.
     * 
     * @throws IOException
     * @throws Exception
     */
    public byte[] receive()
        throws IOException
    {
        byte[] buff = null;
        
        int len = -1;
        
        if (true == socket.isConnected() && false == socket.isClosed())
        {
            byte[] outerHead = new byte[HEADLENGTH];
            try
            {
                len = is.read(outerHead);
                if (len == -1)
                {
                    System.out.println("+++++++++++++++++++++++++++++++++++" + len);
                    throw new IOException();
                }
                OuterHeadPojo headPojo = OuterHeadPojo.getOuterHeadInfo(outerHead);// 获取包头
                
                System.out.println("收到包: " + headPojo.toString());
                
                // --------------------- 命令包头 ---------------------
                byte[] cmdHead = new byte[49];
                len = is.read(cmdHead);
                if (len == -1)
                {
                    System.out.println("+++++++++++++++++++++++++++++++++++" + len);
                    throw new IOException();
                }
                
                // 登录响应
                byte[] loginResponse = new byte[4];
                len = is.read(loginResponse);
                if (len == -1)
                {
                    System.out.println("+++++++++++++++++++++++++++++++++++" + len);
                    throw new IOException();
                }
                buff = loginResponse;
            }
            catch (EOFException e)
            {
                throw e;
            }
            catch (IOException e)
            {
                throw e;
            }
        }
        return buff;
    }
    
    public boolean getPushResponse(int senderID, int receiverID)
        throws IOException
    {
        boolean result = false;
        
        int len = -1;
        
        if (true == socket.isConnected() && false == socket.isClosed())
        {
            byte[] outerHead = new byte[HEADLENGTH];
            try
            {
                len = is.read(outerHead);
                if (len == -1)
                {
                    System.out.println("+++++++++++++++++++++++++++++++++++" + len);
                    throw new IOException();
                }
                OuterHeadPojo headPojo = OuterHeadPojo.getOuterHeadInfo(outerHead);// 获取包头
                
                System.out.println("收到包: " + headPojo.toString());
                
                // --------------------- 命令包头 ---------------------
                byte[] cmdHead = new byte[49];
                len = is.read(cmdHead);
                if (len == -1)
                {
                    System.out.println("+++++++++++++++++++++++++++++++++++" + len);
                    throw new IOException();
                }
                CmdHeadPojo cmdHeadPojo = WebsocketDataProcess.getCmdHeadInfo(cmdHead);
                
                short cmdId = cmdHeadPojo.getUsCmdID();
                if (cmdId == MsgConstant.IM_ACK_PERSONAL_CMD)
                {
                    result = true;
                }
            }
            catch (EOFException e)
            {
                throw e;
            }
            catch (IOException e)
            {
                throw e;
            }
        }
        return result;
    }
    
    /**
     * 
     * @param funcDataLen
     * @return
     * @Description: 设置外层头部
     */
    public byte[] setOuterHead(int funcDataLen)
    {
        OuterHeadPojo outerHeadPojo = new OuterHeadPojo();
        outerHeadPojo.setiDataLen(49 + funcDataLen);
        outerHeadPojo.setiPacketFlag(1097745780);
        return outerHeadPojo.toByte();
    }
    
    /**
     * 
     * @param oneselfIMID
     * @param usCmdId
     * @param senderID
     * @param receiverID
     * @return
     * @Description: 设置命令头部
     */
    public byte[] setCmdHead(long oneselfIMID, int usCmdId, int senderID, int receiverID)
    {
        CmdHeadPojo cmdHeadPojo = new CmdHeadPojo();
        cmdHeadPojo.setOneselfIMID(oneselfIMID);
        cmdHeadPojo.setUsCmdID((short)usCmdId);
        cmdHeadPojo.setUcVer((byte)1);
        cmdHeadPojo.setUcDeviceType((byte)1);
        cmdHeadPojo.setUcFlag((byte)0);
        cmdHeadPojo.setServerTimes(0);
        
        // 信息
        MsgPojo msgPojo = new MsgPojo();
        msgPojo.setSenderID(senderID);
        msgPojo.setReceiverID(receiverID);
        msgPojo.setClientTimes(System.currentTimeMillis());
        Object msgRandVal = JedisClusterHelper.get("msgRandVal");
        if (msgRandVal == null)
        {
            msgRandVal = 1;
        }
        else
        {
            msgRandVal = Integer.parseInt(msgRandVal.toString()) + 1;
            JedisClusterHelper.set("msgRandVal", msgRandVal);
        }
        msgPojo.setRand(Integer.parseInt(msgRandVal.toString()));
        
        cmdHeadPojo.setMsgPojo(msgPojo);
        
        return cmdHeadPojo.toByte();
    }
    
    /**
     * 
     * @param outHeadByte
     * @param cmdHeadByte
     * @param funcByte
     * @return
     * @Description: 合并请求数据
     */
    public byte[] mergeRequestInfo(byte[] outHeadByte, byte[] cmdHeadByte, byte[] funcByte)
    {
        byte[] mergeAfterData = new byte[outHeadByte.length + cmdHeadByte.length + funcByte.length];
        System.arraycopy(outHeadByte, 0, mergeAfterData, 0, outHeadByte.length);
        System.arraycopy(cmdHeadByte, 0, mergeAfterData, outHeadByte.length, cmdHeadByte.length);
        System.arraycopy(funcByte, 0, mergeAfterData, outHeadByte.length + cmdHeadByte.length, funcByte.length);
        return mergeAfterData;
    }
    
}
