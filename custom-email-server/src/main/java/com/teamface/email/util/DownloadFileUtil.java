package com.teamface.email.util;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.OSSUtil;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年3月14日 下午3:21:04
 * @version: 1.0
 */
public class DownloadFileUtil
{
    private Log log = LogFactory.getLog(DownloadFileUtil.class);
    
    private String fileDir;
    
    private InputStream is = null;
    
    private BufferedInputStream fos = null;
    
    private OutputStream os = null;
    
    BufferedOutputStream bio = null;
    
    private File file;
    
    private DownloadFileUtil()
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        fileDir = config.getString("teamface.download.tem");
    }
    
    public static DownloadFileUtil getInstance()
    {
        return new DownloadFileUtil();
    }
    
    /**
     * 
     * @param fileName
     * @return
     * @Description:下载通用接口文件
     */
    public String downloadCommonFile(String fileName)
    {
        String filePath = "";
        try
        {
            // 判断是否存在临时存放目录，没有则新建
            file = new File(fileDir);
            if (!file.exists())
            {
                file.mkdir();
            }
            filePath = new StringBuilder().append(fileDir).append("/").append(fileName).toString();
            String fullPath = filePath.substring(0, filePath.lastIndexOf("/"));
            file = new File(fullPath);
            if (!file.exists())
            {
                file.mkdirs();
            }
            String bucketName = Constant.FLIE_LIBRARY_NAME;
            // 保存附件到路径
            InputStream is = OSSUtil.getInstance().getFile(bucketName, fileName);
            OutputStream fio = null;
            BufferedInputStream bis = null;
            BufferedOutputStream bio = null;
            try
            {
                fio = new FileOutputStream(new File(filePath));
                bis = new BufferedInputStream(is);
                bio = new BufferedOutputStream(fio);
                int len = -1;
                byte[] b = new byte[1024 * 1024];
                while ((len = bis.read(b)) != -1)
                {
                    bio.write(b, 0, len);
                }
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
            }
            finally
            {
                try
                {
                    if (null != fio)
                    {
                        fio.close();
                    }
                    if (null != bis)
                    {
                        bis.close();
                    }
                    if (null != bio)
                    {
                        bio.close();
                    }
                }
                catch (IOException e)
                {
                    log.error(e.getMessage(), e);
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        
        return filePath;
    }
    
    /**
     * 
     * @param id
     * @param companyId
     * @return
     * @Description:下载文件库中文件
     */
    public String downloadLibFile(Long id, Long companyId, String fileName)
    {
        String filePath = "";
        try
        {
            file = new File(fileDir);
            if (!file.exists())
            {
                file.mkdir();
            }
            filePath = new StringBuilder().append(fileDir).append("/").append(fileName).toString();
            StringBuilder queryFileSqlSB = new StringBuilder().append("select * from catalog_").append(companyId).append(" where id = ").append(id);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSqlSB.toString());
            if (null == fileJson)
            {
                return "";
            }
            String fileUrl = fileJson.getString("url");
            is = OSSUtil.getInstance().getFile(Constant.FLIE_LIBRARY_NAME, fileUrl);
            fos = new BufferedInputStream(is);
            os = new FileOutputStream(new File(filePath));
            bio = new BufferedOutputStream(os);
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                bio.write(buffer, 0, count);
            }
            is.close();
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeConnection();
            
        }
        return filePath;
    }
    
    /**
     * 
     * @param companyId
     * @param fileName
     * @param employeeId
     * @return
     * @Description:获取同步附件的路径
     */
    public String getEmailAttachmentDir(Long companyId, String fileName, Long employeeId)
    {
        StringBuilder filePath = new StringBuilder().append(fileDir).append("/").append(companyId);
        filePath.append("/").append(employeeId).append("/").append(fileName);
        return filePath.toString();
    }
    
    /**
     * 
     * @Description:关闭连接
     */
    private void closeConnection()
    {
        try
        {
            if (fos != null)
            {
                fos.close();
            }
            if (is != null)
            {
                is.close();
            }
            if (bio != null)
            {
                bio.close();
            }
            if (os != null)
            {
                os.close();
            }
            
        }
        catch (IOException e)
        {
            e.printStackTrace();
            
        }
    }
}
