package com.teamface.common.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

/**
 * 文件操作
 * 
 */
public class FileUtil
{
    private static final Logger log = Logger.getLogger(FileUtil.class);
    
    private FileUtil()
    {
    }
    
    /**
     * 返回文件的扩展名 不包括"."
     * 
     * @param fileName
     * @return abc.jpg=jpg
     */
    public static String getFileExt(String fileName)
    {
        if (StringUtil.isBlank(fileName))
        {
            return "";
        }
        fileName = fileName.trim();
        int index = fileName.lastIndexOf(".");
        if (index < 0 || fileName.endsWith("."))
        {
            return "";
        }
        return fileName.substring(index + 1, fileName.length());
    }
    
    public static List<String> readFile(File file, String delimiter, String encoding)
    {
        List<String> resultLS = new ArrayList<>();
        
        BufferedReader br = null;
        try
        {
            String str = null;
            StringBuilder contentSB = new StringBuilder();
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), encoding);
            br = new BufferedReader(isr);
            while (true)
            {
                str = br.readLine();
                if (str == null)
                    break;
                if (str.trim().length() > 0)
                {
                    contentSB.append(str);
                    if (str.contains(delimiter))
                    {
                        resultLS.add(contentSB.toString());
                        contentSB.setLength(0);
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (br != null)
                    br.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        
        return resultLS;
    }
    
    /**
     * 
     * @param file 文件资源
     * @param encoding 编码格式
     * @return
     * @Description:读取JSON文件
     */
    public static String readJSONFile(File file, String encoding)
    {
        StringBuilder contentSB = new StringBuilder();
        BufferedReader br = null;
        try
        {
            String str = null;
            InputStreamReader isr = new InputStreamReader(new FileInputStream(file), encoding);
            br = new BufferedReader(isr);
            while (true)
            {
                str = br.readLine();
                if (str == null)
                    break;
                if (str.trim().length() > 0)
                {
                    contentSB.append(str);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (br != null)
                    br.close();
            }
            catch (Exception e)
            {
                log.error(e.getMessage(), e);
            }
        }
        return null == contentSB ? "" : contentSB.toString();
    }
}