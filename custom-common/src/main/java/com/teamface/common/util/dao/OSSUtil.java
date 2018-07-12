package com.teamface.common.util.dao;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.BucketReferer;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.teamface.common.util.PropertiesConfigObject;

/**
 * 
 * @Title:阿里开放式存储（OSS）
 * @Description:
 * @Author:zhangzhihua
 * @Since:2017年9月25日
 * @Version:1.1.0
 */
public class OSSUtil
{
    private static final Logger log = LogManager.getLogger(OSSUtil.class);
    
    private OSSClient client = null;
    
    private List<String> buckets = new ArrayList<>();
    
    private List<String> refererList = new ArrayList<>();
    
    private static volatile OSSUtil instance = null;
    
    public static OSSUtil getInstance()
    {
        if (instance == null)
        {
            synchronized (OSSUtil.class)
            {
                if (instance == null)
                {
                    instance = new OSSUtil();
                }
            }
        }
        return instance;
    }
    
    private OSSUtil()
    {
        try
        {
            PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
            Configuration config = properties.getConfig();
            String accessUrl = config.getString("oss.server.url");
            String accessKey = config.getString("oss.server.key");
            String accessSecret = config.getString("oss.server.secret");
            String bucketReferer = config.getString("oss.bucket.referer");
            int maxConnections = config.getInt("oss.max.connections");
            int maxErrorRetry = config.getInt("oss.max.error.retry");
            int socketTimeout = config.getInt("oss.socket.timeout");
            int connectionTimeout = config.getInt("oss.connection.timeout");
            // 防盗链白名单
            if (!StringUtils.isEmpty(bucketReferer))
            {
                for (String referer : bucketReferer.split(","))
                {
                    refererList.add(referer);
                }
            }
            
            ClientConfiguration conf = new ClientConfiguration();
            // 设置HTTP最大连接数
            conf.setMaxConnections(maxConnections);
            
            // 设置TCP连接超时
            conf.setConnectionTimeout(connectionTimeout);
            
            // 设置最大的重试次数
            conf.setMaxErrorRetry(maxErrorRetry);
            
            // 设置Socket传输数据超时的时间
            conf.setSocketTimeout(socketTimeout);
            client = new OSSClient(accessUrl, accessKey, accessSecret, conf);
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
    }
    
    private void checkBucket(String bucketName)
    {
        if (!buckets.contains(bucketName))
        {
            log.info("bucketName:" + bucketName);
            if (!client.doesBucketExist(bucketName))
            {
                client.createBucket(bucketName);
                client.setBucketAcl(bucketName, CannedAccessControlList.Private);
                if (!refererList.isEmpty())
                {
                    BucketReferer referer = new BucketReferer(false, refererList);
                    client.setBucketReferer(bucketName, referer);
                }
            }
            buckets.add(bucketName);
        }
    }
    
    public boolean existKey(String bucketName, String key)
    {
        if (bucketName == null || bucketName.isEmpty() || key == null || key.isEmpty())
        {
            return false;
        }
        if (!client.doesBucketExist(bucketName))
        {
            return false;
        }
        return client.doesObjectExist(bucketName, key);
    }
    
    public String addFile(String bucketName, File file)
    {
        InputStream content = null;
        try
        {
            if (StringUtils.isEmpty(bucketName) || file == null)
            {
                return null;
            }
            checkBucket(bucketName);
            content = new FileInputStream(file);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.length());
            StringBuilder key = new StringBuilder();
            key.append(file.getName());
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            
            log.info("eTag:" + result.getETag());
            return key.toString();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        finally
        {
            if (content != null)
            {
                try
                {
                    content.close();
                }
                catch (IOException e)
                {
                    log.error(e.toString(), e);
                }
            }
        }
        return null;
    }
    
    public String addFile(String bucketName, InputStream content, String fileName, long size)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || content == null)
            {
                return null;
            }
            checkBucket(bucketName);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            key.append(fileName);
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            
            content.close();
            log.info(result.getETag());
            return key.toString();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public boolean addFile(String bucketName, InputStream content, long size, String key)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || content == null)
            {
                return false;
            }
            checkBucket(bucketName);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            PutObjectResult result = client.putObject(bucketName, key, content, meta);
            
            content.close();
            log.info(result.getETag());
            return true;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return false;
    }
    
    public boolean addFolder(String bucketName, String folderPath)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || folderPath == null)
            {
                return false;
            }
            checkBucket(bucketName);
            byte[] buffer = new byte[0];
            ByteArrayInputStream in = new ByteArrayInputStream(buffer);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(0);
            client.putObject(bucketName, folderPath, in, meta);
            return true;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return false;
    }
    
    public InputStream getFile(String bucketName, String key)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(key))
            {
                return null;
            }
            OSSObject object = client.getObject(bucketName, key);
            return object.getObjectContent();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public String getUrl(String bucketName, String key, Date expiration)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(key) || expiration.before(new Date()))
            {
                return null;
            }
            URL url = client.generatePresignedUrl(bucketName, key, expiration);
            return url.toString();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public InputStream getCompressedFile(String bucketName, String key, Long width, Long height)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(key))
            {
                return null;
            }
            // 缩放
            StringBuilder style = new StringBuilder();
            if (null == height)
            {
                style.append("image/resize,w_").append(width);
            }
            if (null != width && null != height)
            {
                style.append("image/resize,m_fixed,w_").append(width).append(",h_").append(height);
            }
            GetObjectRequest request = new GetObjectRequest(bucketName, key);
            request.setProcess(style.toString());
            OSSObject object = client.getObject(request);
            return object.getObjectContent();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
        
    }
    
    public boolean deleteObject(String bucketName, String key)
    {
        try
        {
            if (StringUtils.isEmpty(bucketName) || StringUtils.isEmpty(key))
            {
                return false;
            }
            client.deleteObject(bucketName, key);
            return true;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return false;
    }
    
    public boolean copyObject(String sourceBucketName, String sourceKey, String destinationBucketName, String destinationKey)
    {
        try
        {
            if (StringUtils.isEmpty(sourceBucketName) || StringUtils.isEmpty(sourceKey) || StringUtils.isEmpty(destinationBucketName) || StringUtils.isEmpty(destinationKey))
            {
                return false;
            }
            client.copyObject(sourceBucketName, sourceKey, destinationBucketName, destinationKey);
            return true;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return false;
    }
}