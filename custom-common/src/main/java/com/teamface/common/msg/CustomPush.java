package com.teamface.common.msg;

import java.io.IOException;
import java.net.SocketException;
import java.net.SocketTimeoutException;

import com.teamface.common.msg.constant.MsgConstant;
import com.teamface.common.msg.pojo.LoginRequestPojo;
import com.teamface.common.msg.util.ParseTxtFromFile;
import com.teamface.common.util.dao.JedisClusterHelper;

/**
 * @Description: 自定义推送
 * @author: chenxiaomin
 * @date: 2017年10月12日 下午2:14:30
 * @version: 1.0
 */
public class CustomPush
{
    
    private static boolean isLogin = false;
    
    /**
     * 
     * @throws SocketTimeoutException
     * @throws SocketException
     * @throws IOException
     * @Description: 登录
     */
    public void login()
        throws SocketTimeoutException, SocketException, IOException
    {
        
        if (isLogin)
        {
            return;
        }
        
        TcpClientSocket client = TcpClientSocket.getInstance();
        
        // 外层包头
        byte[] outerHead = client.setOuterHead(52);
        
        // 命令包头
        byte[] cmdHead = client.setCmdHead(110, MsgConstant.IM_LOG_IN_CMD, 110, 0);
        
        // 登录
        LoginRequestPojo loginRequestPojo = new LoginRequestPojo();
        loginRequestPojo.setSzUsername("hcsong");
        loginRequestPojo.setChStatus((byte)1);
        loginRequestPojo.setChUserType((byte)1);
        byte[] loginReq = loginRequestPojo.toByte();
        
        // 请求数据
        byte[] reqData = client.mergeRequestInfo(outerHead, cmdHead, loginReq);
        
        // 发送请求
        client.send(reqData);
        
        // 返回结果
        byte[] result = client.receive();
        int loginResult = ParseTxtFromFile.lBytesToInt(result);
        if (loginResult == 0)
        {
            // 将我自己的imid放入redis中
            JedisClusterHelper.set("oneselfIMID", 110L);
        }
        isLogin = true;
        System.out.println("result = " + loginResult);
    }
    
    /**
     * 
     * @param oneselfIMID
     * @param senderID
     * @param receiverID
     * @throws SocketTimeoutException
     * @throws SocketException
     * @throws IOException
     * @Description: 发送单条推送
     */
    public boolean singlePush(String content, int senderID, int receiverID)
        throws SocketTimeoutException, SocketException, IOException
    {
        
        boolean pushResult = false;
        
        // 返回执行结果
        return pushResult;
    }
    
    public static void main(String[] args)
        throws SocketTimeoutException, SocketException, IOException
    {
        CustomPush customPush = new CustomPush();
        boolean result = customPush.singlePush("1111", 110, 111);
        System.out.println("推送结果：" + result);
        
    }
}
