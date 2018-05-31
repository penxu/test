package com.teamface.common.msg.util;

import java.io.UnsupportedEncodingException;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class ParseTxtFromFile
{
    private static final Logger log = LogManager.getLogger(ParseTxtFromFile.class);
    
    // int 转换成byte数组
    public static byte[] toLH(int n)
    {
        byte[] b = new byte[4];
        b[0] = (byte)(n & 0xff);
        b[1] = (byte)(n >> 8 & 0xff);
        b[2] = (byte)(n >> 16 & 0xff);
        b[3] = (byte)(n >> 24 & 0xff);
        return b;
    }
    
    // short 转换成byte数组
    public static byte[] toLH(short n)
    {
        byte[] b = new byte[2];
        b[0] = (byte)(n & 0xff);
        b[1] = (byte)(n >> 8 & 0xff);
        return b;
    }
    
    // byte 数组与 long 的相互转换
    public static byte[] longToBytes(long n)
    {
        byte[] b = new byte[8];
        b[0] = (byte)(n & 0xff);
        b[1] = (byte)(n >> 8 & 0xff);
        b[2] = (byte)(n >> 16 & 0xff);
        b[3] = (byte)(n >> 24 & 0xff);
        b[4] = (byte)(n >> 32 & 0xff);
        b[5] = (byte)(n >> 40 & 0xff);
        b[6] = (byte)(n >> 48 & 0xff);
        b[7] = (byte)(n >> 56 & 0xff);
        return b;
    }
    
    // byte转换成byte数组(其实,可不要)
    public static byte[] toLH(byte n)
    {
        byte[] b = new byte[1];
        b[0] = n;
        return b;
        
    }
    
    // 三个BYTE值转换成一个BYTE值(6:1:1)
    public static byte[] toLH(byte a, byte b, byte c)
    {
        byte[] bb = new byte[1];
        bb[0] = (byte)(a + (b << 6) + (c << 7));
        return bb;
    }
    
    // 一个BYTE值转换成三个BYTE值(6:1:1)
    public static byte[] toLH1(byte b)
    {
        byte[] bb = new byte[3];
        
        bb[0] = (byte)(b & ~(3 << 6));
        bb[1] = (byte)((b & (1 << 6)) >> 6);
        bb[2] = (byte)((b & (1 << 7)) >> 7);
        return bb;
    }
    
    // byte数组转换成INT
    public static int lBytesToInt(byte[] b)
    {
        int s = 0;
        for (int i = 0; i < 3; ++i)
        {
            if (b[3 - i] >= 0)
                s = s + b[3 - i];
            else
                s = s + 256 + b[3 - i];
                
            s = s * 256;
        }
        
        if (b[0] >= 0)
            s = s + b[0];
        else
            s = s + 256 + b[0];
            
        return s;
    }
    
    // byte数组转换成SHORT
    public static short lBytesToShort(byte[] b)
    {
        int s = 0;
        if (b[1] >= 0)
            s = s + b[1];
        else
            s = s + 256 + b[1];
        s = s * 256;
        
        if (b[0] >= 0)
            s = s + b[0];
        else
            s = s + 256 + b[0];
            
        short result = (short)s;
        return result;
    }
    
    public static long lBytesToLong(byte[] b)
    {
        long s = 0;
        for (int i = 0; i < 7; ++i)
        {
            if (b[7 - i] >= 0)
                s = s + b[7 - i];
            else
                s = s + 256 + b[7 - i];
                
            s = s * 256;
        }
        
        if (b[0] >= 0)
            s = s + b[0];
        else
            s = s + 256 + b[0];
        return s;
    }
    
    /**
     * 将byte 转换成数组
     * 
     * @param b 源数据
     * @param start 开始位置
     * @param len 长度
     * @param charset 编码格式 默认GBK
     * @return
     */
    public static String lBytesToString(byte[] b, int start, int len, String charset)
    {
        if (charset == null || charset.trim().length() == 0)
        {
            charset = "GBK";
        }
        int count = 0;
        for (int i = 0; i < len; i++)
        {
            
            if (b[i + start] == 0)
            {
                break;
            }
            count++;
            
        }
        byte[] temp = new byte[count];
        for (int i = 0; i < count; i++)
        {
            
            temp[i] = b[i + start];
        }
        
        try
        {
            return new String(temp, charset);
        }
        catch (UnsupportedEncodingException e)
        {
            log.error(e.getMessage(), e);
        }
        return "";
    }
    
}