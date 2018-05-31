package com.teamface.common.msg.pojo;

import com.teamface.common.msg.util.ParseTxtFromFile;

/**
 * @Description: 命令包头
 * @author: chenxiaomin
 * @date: 2017年10月11日 下午2:24:08
 * @version: 1.0
 */
public class CmdHeadPojo
{
    /** 自己的IMID */
    private long oneselfIMID;
    
    /** 命令类型ID */
    private short usCmdID;
    
    /** 版本号 */
    private byte ucVer;
    
    /** 设备类型 */
    private byte ucDeviceType;
    
    /** 标志位 ：IM_TYPE_REQUEST---请求 IM_TYPE_RESPONSE---响应 */
    private byte ucFlag;
    
    /** 服务器时间戳.由服务器来填充 */
    private long serverTimes;
    
    /** 消息 */
    private MsgPojo msgPojo;
    
    public long getOneselfIMID()
    {
        return oneselfIMID;
    }
    
    public void setOneselfIMID(long oneselfIMID)
    {
        this.oneselfIMID = oneselfIMID;
    }
    
    public short getUsCmdID()
    {
        return usCmdID;
    }
    
    public void setUsCmdID(short usCmdID)
    {
        this.usCmdID = usCmdID;
    }
    
    public byte getUcVer()
    {
        return ucVer;
    }
    
    public void setUcVer(byte ucVer)
    {
        this.ucVer = ucVer;
    }
    
    public byte getUcDeviceType()
    {
        return ucDeviceType;
    }
    
    public void setUcDeviceType(byte ucDeviceType)
    {
        this.ucDeviceType = ucDeviceType;
    }
    
    public byte getUcFlag()
    {
        return ucFlag;
    }
    
    public void setUcFlag(byte ucFlag)
    {
        this.ucFlag = ucFlag;
    }
    
    public long getServerTimes()
    {
        return serverTimes;
    }
    
    public void setServerTimes(long serverTimes)
    {
        this.serverTimes = serverTimes;
    }
    
    public MsgPojo getMsgPojo()
    {
        return msgPojo;
    }
    
    public void setMsgPojo(MsgPojo msgPojo)
    {
        this.msgPojo = msgPojo;
    }
    
    public byte[] toByte()
    {
        byte[] b = new byte[49];
        byte[] temp;
        // int 4字节数组
        temp = ParseTxtFromFile.longToBytes(getOneselfIMID());
        System.arraycopy(temp, 0, b, 0, temp.length);
        
        temp = ParseTxtFromFile.toLH(getUsCmdID());
        System.arraycopy(temp, 0, b, 8, temp.length);
        
        temp = ParseTxtFromFile.toLH(getUcVer());
        System.arraycopy(temp, 0, b, 10, temp.length);
        
        temp = ParseTxtFromFile.toLH(getUcDeviceType());
        System.arraycopy(temp, 0, b, 11, temp.length);
        
        temp = ParseTxtFromFile.toLH(getUcFlag());
        System.arraycopy(temp, 0, b, 12, temp.length);
        
        temp = new byte[8];
        System.arraycopy(temp, 0, b, 13, temp.length);
        
        temp = ParseTxtFromFile.longToBytes(msgPojo.getSenderID());
        System.arraycopy(temp, 0, b, 21, temp.length);
        
        temp = ParseTxtFromFile.longToBytes(msgPojo.getReceiverID());
        System.arraycopy(temp, 0, b, 29, temp.length);
        
        temp = ParseTxtFromFile.longToBytes(msgPojo.getClientTimes());
        System.arraycopy(temp, 0, b, 37, temp.length);
        
        temp = ParseTxtFromFile.toLH(msgPojo.getRand());
        System.arraycopy(temp, 0, b, 45, temp.length);
        
        return b;
    }
    
}
