package com.teamface.common.util.dao;

import java.io.File;

import org.artofsolving.jodconverter.OfficeDocumentConverter;
import org.artofsolving.jodconverter.office.DefaultOfficeManagerConfiguration;
import org.artofsolving.jodconverter.office.OfficeManager;

import com.teamface.common.constant.Constant;
import com.teamface.common.msg.WebSocketCustomPush;

/**
 * @Description:
 * @author: mofan
 * @date: 2018年3月5日 下午2:10:41
 * @version: 1.0
 */

public class Office2Pdf
{
    public static void word2pdf(String inputFilePath)
    {

//        new Thread()
//        {
//            @Override
//            public void run()
//            {
//                
                DefaultOfficeManagerConfiguration config = new DefaultOfficeManagerConfiguration();
                // int port = 8100;
                // config.setPortNumbers(port); //设置转换端口，默认为8100  
                // config.setTaskExecutionTimeout(1000 * 60 * 25L);//设置任务执行超时为25分钟  
                // config.setTaskQueueTimeout(1000 * 60 * 60 * 24L);//设置任务队列超时为24小时     
                String officeHome = getOfficeHome();
                config.setOfficeHome(officeHome);
                OfficeManager officeManager = config.buildOfficeManager();
                officeManager.start();
                OfficeDocumentConverter converter = new OfficeDocumentConverter(officeManager);
                String outputFilePath = getOutputFilePath(inputFilePath);
                File inputFile = new File(inputFilePath);
                if (inputFile.exists())
                {// 找不到源文件, 则返回
                    File outputFile = new File(outputFilePath);
                    if (!outputFile.getParentFile().exists())
                    { // 假如目标路径不存在, 则新建该路径
                        outputFile.getParentFile().mkdirs();
                    }
                    converter.convert(inputFile, outputFile);
                }
                officeManager.stop();
//            }
//        }.start();
    }
    
    public static String getOutputFilePath(String inputFilePath)
    {
        String sourceStuff = inputFilePath.substring(inputFilePath.lastIndexOf("."));
        String outputFilePath = inputFilePath.replaceAll(sourceStuff, ".pdf");
        return outputFilePath;
    }
    
    public static void main(String[] args)
    {
        
        word2pdf("/home/app/1.xlsx");
        
    }
    
    public static String getOfficeHome()
    {
        
        return "/opt/openoffice4";
        // String osName = System.getProperty("os.name");
        // if (Pattern.matches("Linux.*", osName)) {
        // return "/opt/openoffice.org3";
        // } else if (Pattern.matches("Windows.*", osName)) {
        // return "D:/Program Files/OpenOffice.org 3";
        // } else if (Pattern.matches("Mac.*", osName)) {
        // return "/Application/OpenOffice.org.app/Contents";
        // }
        // return null;
    }
}
