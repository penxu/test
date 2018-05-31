package com.teamface.common.msg.util;

import com.teamface.common.msg.pojo.CmdHeadPojo;
import com.teamface.common.msg.pojo.MsgPojo;
import com.teamface.common.msg.pojo.OuterHeadPojo;
import com.teamface.common.util.dao.JedisClusterHelper;

public class WebsocketDataProcess
{
    /**
     * 
     * @param funcDataLen
     * @return
     * @Description:外部包头转字节数组
     */
    public static byte[] setOuterHead(int funcDataLen)
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
     * @param ucDeviceType
     * @return
     * @Description:命令包头转字节数组
     */
    public static byte[] setCmdHead(long oneselfIMID, long usCmdId, long senderID, long receiverID, int ucDeviceType)
    {
        CmdHeadPojo cmdHeadPojo = new CmdHeadPojo();
        cmdHeadPojo.setOneselfIMID(oneselfIMID);
        cmdHeadPojo.setUsCmdID((short)usCmdId);
        cmdHeadPojo.setUcVer((byte)1);
        cmdHeadPojo.setUcDeviceType((byte)ucDeviceType);
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
     * @Description:外部包头，命令包头和发送内容转字节数组
     */
    public static byte[] mergeRequestInfo(byte[] outHeadByte, byte[] cmdHeadByte, byte[] funcByte)
    {
        byte[] mergeAfterData = new byte[outHeadByte.length + cmdHeadByte.length + funcByte.length];
        System.arraycopy(outHeadByte, 0, mergeAfterData, 0, outHeadByte.length);
        System.arraycopy(cmdHeadByte, 0, mergeAfterData, outHeadByte.length, cmdHeadByte.length);
        System.arraycopy(funcByte, 0, mergeAfterData, outHeadByte.length + cmdHeadByte.length, funcByte.length);
        return mergeAfterData;
    }
    
    /**
     * 
     * @param cmdHeadByte
     * @param funcByte
     * @return
     * @Description:命令包头和发送内容转字节数组
     */
    public static byte[] mergeRequestInfo(byte[] cmdHeadByte, byte[] funcByte)
    {
        byte[] mergeAfterData = new byte[cmdHeadByte.length + funcByte.length];
        System.arraycopy(cmdHeadByte, 0, mergeAfterData, 0, cmdHeadByte.length);
        System.arraycopy(funcByte, 0, mergeAfterData, cmdHeadByte.length, funcByte.length);
        return mergeAfterData;
    }
    
    public static CmdHeadPojo getCmdHeadInfo(byte[] b)
    {
        
        CmdHeadPojo cmdHeadPojo = new CmdHeadPojo();
        byte[] temp = new byte[8];
        for (int i = 0; i < 8; i++)
        {
            temp[i] = b[i + 0];
        }
        cmdHeadPojo.setOneselfIMID(ParseTxtFromFile.lBytesToLong(temp));
        
        for (int i = 0; i < 2; i++)
        {
            temp[i] = b[i + 8];
        }
        cmdHeadPojo.setUsCmdID(ParseTxtFromFile.lBytesToShort(temp));
        cmdHeadPojo.setUcVer(b[10]);
        cmdHeadPojo.setUcDeviceType(b[11]);
        cmdHeadPojo.setUcFlag(b[12]);
        cmdHeadPojo.setServerTimes(0);
        
        MsgPojo msgPojo = new MsgPojo();
        for (int i = 0; i < 8; i++)
        {
            temp[i] = b[i + 21];
        }
        msgPojo.setSenderID(ParseTxtFromFile.lBytesToLong(temp));
        
        for (int i = 0; i < 8; i++)
        {
            temp[i] = b[i + 29];
        }
        msgPojo.setReceiverID(ParseTxtFromFile.lBytesToLong(temp));
        for (int i = 0; i < 8; i++)
        {
            temp[i] = b[i + 37];
        }
        msgPojo.setClientTimes(ParseTxtFromFile.lBytesToLong(temp));
        for (int i = 0; i < 4; i++)
        {
            temp[i] = b[i + 45];
        }
        msgPojo.setRand(ParseTxtFromFile.lBytesToInt(temp));
        cmdHeadPojo.setMsgPojo(msgPojo);
        
        return cmdHeadPojo;
    }
}
