package com.teamface.common.util.dao;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONObject;
import com.aliyun.oss.ClientConfiguration;
import com.aliyun.oss.ClientException;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.model.BucketReferer;
import com.aliyun.oss.model.CannedAccessControlList;
import com.aliyun.oss.model.GetObjectRequest;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.ObjectMetadata;
import com.aliyun.oss.model.PutObjectResult;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

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
    
    private static OSSUtil instance = null;
    
    private OSSClient client = null;
    
    private String ffmpegUrl = "";
    
    private String fileUrl = "";
    
    private String vedioFileUrl = "";
    
    private String imageFileUrl = "";
    
    private String emailFileUrl = "";
    
    private String pdfFileWindowTempUrl = "";
    
    private String pdfFileLinuxTempUrl = "";
    
    private List<String> refererList = new ArrayList<>();
    
    public static synchronized OSSUtil getInstance()
    {
        if (instance == null)
        {
            instance = new OSSUtil();
        }
        return instance;
    }
    
    private OSSUtil()
    {
        try
        {
            PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
            Configuration config = properties.getConfig();
            ffmpegUrl = config.getString("ffmpeg.linux.url");
            fileUrl = config.getString("teamface.file.url");
            vedioFileUrl = config.getString("teamface.openmovie.url");
            imageFileUrl = config.getString("teamface.imagefile.url");
            emailFileUrl = config.getString("teamface.emailfile.url");
            pdfFileWindowTempUrl = config.getString("teamface.download.window.pdfdir");
            pdfFileLinuxTempUrl = config.getString("teamface.download.linux.pdfdir");
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
        try
        {
            if (bucketName == null || bucketName.isEmpty() || file == null)
            {
                return null;
            }
            if (!client.doesBucketExist(bucketName))
            {
                client.createBucket(bucketName);
                client.setBucketAcl(bucketName, CannedAccessControlList.Private);
                BucketReferer referer = new BucketReferer(refererList.isEmpty(), refererList);
                client.setBucketReferer(bucketName, referer);
            }
            InputStream content = new FileInputStream(file);
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(file.length());
            StringBuilder key = new StringBuilder();
            key.append(file.getName()).append(".").append(System.currentTimeMillis());
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            
            content.close();
            log.info(result.getETag());
            return fileUrl + key;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public String addFile(String bucketName, InputStream content, String fileName, long size)
    {
        try
        {
            if (bucketName == null || bucketName.isEmpty() || content == null)
            {
                return null;
            }
            if (!client.doesBucketExist(bucketName))
            {
                client.createBucket(bucketName);
                client.setBucketAcl(bucketName, CannedAccessControlList.Private);
                BucketReferer referer = new BucketReferer(refererList.isEmpty(), refererList);
                client.setBucketReferer(bucketName, referer);
            }
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            key.append(System.currentTimeMillis()).append(".").append(fileName);
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            
            content.close();
            log.info(result.getETag());
            return fileUrl + "bean=" + bucketName + "&fileName=" + key;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public Map<String, String> addFile(String bucketName, MultipartFile fromFile, String bean, String fileName, long size, String contentType, Long companyId,
        HttpServletRequest request)
    {
        Map<String, String> resultMap = new HashMap<String, String>();
        InputStream content = null;
        InputStream inputContent = null;
        try
        {
            content = fromFile.getInputStream();
            inputContent = fromFile.getInputStream();
            if (bucketName == null || bucketName.isEmpty() || content == null)
            {
                return null;
            }
            if (!client.doesBucketExist(bucketName))
            {
                client.createBucket(bucketName);
                client.setBucketAcl(bucketName, CannedAccessControlList.Private);
            }
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            key.append(companyId).append("/").append(bean).append("/").append(System.currentTimeMillis()).append(".").append(fileName);
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            log.info(result.getETag());
            if (!StringUtils.isEmpty(contentType) && (contentType.startsWith("audio") || contentType.startsWith("video")))
            {
                String url = vedioFileUrl + "bean=" + bean + "&fileName=" + key + "&contentType=" + contentType + "&fileSize=" + size;
                resultMap.put("fileUrl", url);
                String strDirPath = request.getSession().getServletContext().getRealPath("/");
                String upFilePath = strDirPath + File.separator + fileName;
                File movieFile = new File(upFilePath);
                FileUtils.copyInputStreamToFile(inputContent, movieFile);
                String imageMat = "png";
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                fileName += "." + imageMat;
                String mediaPicPath = strDirPath + File.separator + fileName;
                String OS = System.getProperty("os.name").toLowerCase();
                String ffmpegPath = "";
                
                if (OS.indexOf("linux") >= 0)
                {
                    ffmpegPath = ffmpegUrl;
                }
                else
                {
                    ffmpegPath = strDirPath + "//ffmpeg//bin//ffmpeg.exe";
                }
                List cutpic = new ArrayList();
                cutpic.add(ffmpegPath); // 视频提取工具的位置
                cutpic.add("-i"); // 添加参数＂-i＂，该参数指定要转换的文件
                cutpic.add(upFilePath); // 视频文件路径
                cutpic.add("-y");
                cutpic.add("-f");
                cutpic.add("image2");
                cutpic.add("-ss"); // 添加参数＂-ss＂，该参数指定截取的起始时间
                cutpic.add("1"); // 添加起始时间为第1秒
                cutpic.add("-t"); // 添加参数＂-t＂，该参数指定持续时间
                cutpic.add("0.001"); // 添加持续时间为1毫秒
                cutpic.add("-s"); // 添加参数＂-s＂，该参数指定截取的图片大小
                cutpic.add("800*600"); // 添加截取的图片大小为800*600
                cutpic.add(mediaPicPath); // 添加截取的图片的保存路径
                
                ProcessBuilder builder = new ProcessBuilder();
                try
                {
                    
                    builder.command(cutpic);
                    builder.redirectErrorStream(true);
                    // 如果此属性为 true，则任何由通过此对象的 start()
                    // 方法启动的后续子进程生成的错误输出都将与标准输出合并，
                    // 因此两者均可使用 Process.getInputStream()
                    // 方法读取。这使得关联错误消息和相应的输出变得更容易
                    builder.start();
                    
                }
                catch (IOException e)
                {
                    log.error(e.toString(), e);
                    e.printStackTrace();
                }
                
                StringBuilder videokey = new StringBuilder();
                videokey.append(companyId).append("/").append(bean).append("/").append(System.currentTimeMillis()).append(".").append(fileName);
                File picfile = new File(mediaPicPath);
                String thum_url = "";
                if (picfile.exists())
                {
                    PutObjectResult result2 = client.putObject(bucketName, videokey.toString(), picfile);
                    log.info(result2.getETag());
                    thum_url = fileUrl + "bean=" + bean + "&fileName=" + videokey + "&fileSize=" + size;
                    picfile.delete();
                }
                else
                {
                    picfile.createNewFile();
                    PutObjectResult result2 = client.putObject(bucketName, videokey.toString(), picfile);
                    log.info(result2.getETag());
                    thum_url = fileUrl + "bean=" + bean + "&fileName=" + videokey + "&fileSize=" + size;
                    picfile.delete();
                }
                File upFile = new File(upFilePath);
                if (upFile.exists())
                {
                    upFile.delete();
                }
                resultMap.put("video_thumbnail_url", thum_url);
                
            }
            else
            {
                String url = fileUrl + "bean=" + bean + "&fileName=" + key + "&fileSize=" + size;
                resultMap.put("fileUrl", url);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            log.error(e.toString(), e);
        }
        finally
        {
            try
            {
                if (content != null)
                {
                    content.close();
                }
                if (inputContent != null)
                {
                    inputContent.close();
                }
                
            }
            catch (IOException e)
            {
            
            }
        }
        return resultMap;
    }
    
    /**
     * 公司初始化上传的文件
     * 
     * @param bucketName
     * @param content
     * @param bean
     * @param fileName
     * @param size
     * @param contentType
     * @return
     * @Description:
     */
    public String addImageFile(String bucketName, InputStream content, String bean, String fileName, long size, String contentType, Long accountId)
    {
        try
        {
            if (bucketName == null || bucketName.isEmpty() || content == null)
            {
                return null;
            }
            if (!client.doesBucketExist(bucketName))
            {
                client.createBucket(bucketName);
                client.setBucketAcl(bucketName, CannedAccessControlList.Private);
            }
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            key.append(accountId).append("/").append(bean).append("/").append(System.currentTimeMillis()).append(".").append(fileName);
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            
            content.close();
            log.info(result.getETag());
            return imageFileUrl + "bean=" + bean + "&fileName=" + key;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public void getFile(String bucketName, String key, String fullPath)
    {
        try
        {
            
            OSSObject object = client.getObject(bucketName, key);
            InputStream objectContent = object.getObjectContent();
            OutputStream fio = new FileOutputStream(new File(fullPath));
            BufferedInputStream bis = new BufferedInputStream(objectContent);
            BufferedOutputStream bio = new BufferedOutputStream(fio);
            
            int len = -1;
            byte[] b = new byte[1024 * 1024];
            
            while ((len = bis.read(b)) != -1)
            {
                bio.write(b, 0, len);
            }
            
            objectContent.close();
            fio.close();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
    }
    
    public InputStream getFile(String bucketName, String key)
    {
        try
        {
            OSSObject object = client.getObject(bucketName, key);
            return object.getObjectContent();
            
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public InputStream getCompressedFile(String bucketName, String key, Long width, Long height)
    {
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
    
    /**
     * 文件库公共创建文件夹
     * 
     * @param bucketName
     * @return
     */
    public boolean createLibraryFile(String bucketName)
    {
        // 要创建的文件夹名称,在满足Object命名规则的情况下以"/"结尾
        ObjectMetadata objectMeta = new ObjectMetadata();
        /*
         * 这里的size为0,注意OSS本身没有文件夹的概念,这里创建的文件夹本质上是一个size为0的Object,
         * dataStream仍然可以有数据
         * 照样可以上传下载,只是控制台会对以"/"结尾的Object以文件夹的方式展示,用户可以利用这种方式来实现文件夹模拟功能,创建形式上的文件夹
         */
        if (!client.doesBucketExist(Constant.FLIE_LIBRARY_NAME))
        {
            client.createBucket(Constant.FLIE_LIBRARY_NAME);
            client.setBucketAcl(Constant.FLIE_LIBRARY_NAME, CannedAccessControlList.Private);
        }
        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        objectMeta.setContentLength(0);
        try
        {
            client.putObject(Constant.FLIE_LIBRARY_NAME, bucketName, in, objectMeta);
            /* client.deleteObject(bucketName, objectName); */
            // client.copyObject("filesys9", objectName, "filesys9",
            // "jyltest/bb/cc/dd/");
        }
        catch (ClientException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
     * 项目文库公共创建文件夹
     * 
     * @param bucketName
     * @return
     */
    public boolean createProjectLibrary(String bucketName)
    {
        // 要创建的文件夹名称,在满足Object命名规则的情况下以"/"结尾
        ObjectMetadata objectMeta = new ObjectMetadata();
        /*
         * 这里的size为0,注意OSS本身没有文件夹的概念,这里创建的文件夹本质上是一个size为0的Object,
         * dataStream仍然可以有数据
         * 照样可以上传下载,只是控制台会对以"/"结尾的Object以文件夹的方式展示,用户可以利用这种方式来实现文件夹模拟功能,创建形式上的文件夹
         */
        if (!client.doesBucketExist(Constant.PROJECT_LIBRARY_NAME))
        {
            client.createBucket(Constant.PROJECT_LIBRARY_NAME);
            client.setBucketAcl(Constant.PROJECT_LIBRARY_NAME, CannedAccessControlList.Private);
        }
        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        objectMeta.setContentLength(0);
        try
        {
            client.putObject(Constant.PROJECT_LIBRARY_NAME, bucketName, in, objectMeta);
        }
        catch (ClientException e)
        {
            log.error(e.getMessage(), e);
            return false;
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                log.error(e.getMessage(), e);
                return false;
            }
        }
        return true;
    }
    
    /**
     * 公共删除文件夹
     * 
     * @param bucketName
     * @return
     */
    public boolean delLibraryFile(String bucketName)
    {
        // 要创建的文件夹名称,在满足Object命名规则的情况下以"/"结尾
        ObjectMetadata objectMeta = new ObjectMetadata();
        /*
         * 这里的size为0,注意OSS本身没有文件夹的概念,这里创建的文件夹本质上是一个size为0的Object,
         * dataStream仍然可以有数据
         * 照样可以上传下载,只是控制台会对以"/"结尾的Object以文件夹的方式展示,用户可以利用这种方式来实现文件夹模拟功能,创建形式上的文件夹
         */
        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        objectMeta.setContentLength(0);
        try
        {
            // client.copyObject("filesys9", objectName, "filesys9",
            // "jyltest/bb/cc/dd/");
            client.deleteObject(Constant.FLIE_LIBRARY_NAME, bucketName);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
     * 复制文件夹
     * 
     * @param bucketName
     * @return
     */
    public boolean copyLibraryFile(String bucketName, String wornName)
    {
        // 要创建的文件夹名称,在满足Object命名规则的情况下以"/"结尾
        ObjectMetadata objectMeta = new ObjectMetadata();
        /*
         * 这里的size为0,注意OSS本身没有文件夹的概念,这里创建的文件夹本质上是一个size为0的Object,
         * dataStream仍然可以有数据
         * 照样可以上传下载,只是控制台会对以"/"结尾的Object以文件夹的方式展示,用户可以利用这种方式来实现文件夹模拟功能,创建形式上的文件夹
         */
        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        objectMeta.setContentLength(0);
        try
        {
            client.copyObject(Constant.FLIE_LIBRARY_NAME, bucketName, Constant.FLIE_LIBRARY_NAME, wornName);
        }
        finally
        {
            try
            {
                in.close();
                
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    /**
     * 复制文件夹
     * 
     * @param bucketName
     * @return
     */
    public boolean shiftLibraryFile(String bucketName, String wornName)
    {
        // 要创建的文件夹名称,在满足Object命名规则的情况下以"/"结尾
        ObjectMetadata objectMeta = new ObjectMetadata();
        /*
         * 这里的size为0,注意OSS本身没有文件夹的概念,这里创建的文件夹本质上是一个size为0的Object,
         * dataStream仍然可以有数据
         * 照样可以上传下载,只是控制台会对以"/"结尾的Object以文件夹的方式展示,用户可以利用这种方式来实现文件夹模拟功能,创建形式上的文件夹
         */
        byte[] buffer = new byte[0];
        ByteArrayInputStream in = new ByteArrayInputStream(buffer);
        objectMeta.setContentLength(0);
        try
        {
            client.copyObject(Constant.FLIE_LIBRARY_NAME, bucketName, Constant.FLIE_LIBRARY_NAME, wornName);
            client.deleteObject(Constant.FLIE_LIBRARY_NAME, bucketName);
        }
        finally
        {
            try
            {
                in.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }
    
    public String addApplyFile(InputStream content, String beanName, String name, long size, String contentType, String applyId, String token, String modelId, String data_id)
    {
        try
        {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(token);
            String siffix = name.substring(name.lastIndexOf("."));
            key.append(applyId + Constant.APPLY_NAME).append("/").append(beanName + "/").append(System.currentTimeMillis()).append(siffix);
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into catalog_")
                .append(info.getCompanyId())
                .append("(model_id,parent_id,name,size,table_id,sign,url,siffix,type,create_by,create_time) values(")
                .append(data_id)
                .append(",")
                .append(modelId)
                .append(",'")
                .append(name)
                .append("',")
                .append(size)
                .append(",2,")
                .append(Constant.CURRENCY_ONE)
                .append(",'")
                .append(key.toString())
                .append("','")
                .append(siffix)
                .append("',2,")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            DAOUtil.executeUpdate(insertBuilder.toString());
            PutObjectResult result = client.putObject(Constant.FLIE_LIBRARY_NAME, key.toString(), content, meta);
            log.info(result.getETag());
            content.close();
            return fileUrl + "bean=" + beanName + "&fileName=" + key.toString();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    /**
     * 公司个人
     * 
     * @param inputStream
     * @param url
     * @param fileNameNew
     * @param size
     * @param companyId
     * @param fileNameNew
     * @param type
     * @param name
     * @return
     */
    public synchronized boolean savaFileLibraryFile(InputStream content, String id, String url, String fileNameNew, long size, String token, String type)
    {
        
        try
        {
            log.debug("oss链接进入上传=====================================");
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(token);
            String postfix = fileNameNew.substring(fileNameNew.lastIndexOf("."));
            if (Integer.parseInt(type) == 1) // 公司文件
            {
                key.append(url).append(System.currentTimeMillis()).append(postfix);
            }
            else if (Integer.parseInt(type) == 3)// 个人文件
            {
                if (StringUtils.isBlank(id))// 是否是根目录上传
                {
                    Long logId = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
                    logId = logId + 1;
                    id = null;
                    key.append(Constant.PERSONAL_NAME + logId).append("/").append(System.currentTimeMillis()).append(postfix);
                }
                else
                {
                    key.append(url).append(System.currentTimeMillis()).append(postfix);
                }
            }
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into catalog_")
                .append(info.getCompanyId())
                .append("(parent_id,name,size,table_id,url,sign,siffix,create_by,create_time) values(")
                .append(id)
                .append(",'")
                .append(fileNameNew)
                .append("',")
                .append(size)
                .append(",")
                .append(type)
                .append(",'")
                .append(key.toString())
                .append("',")
                .append(Constant.CURRENCY_ONE + ",'")
                .append(postfix)
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int count = DAOUtil.executeUpdate(insertBuilder.toString());
            if (count <= 0)
            {
                return false;
            }
            log.debug("插入主表数据成功=====================================");
            Long currId = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
            StringBuilder savaBuilder = new StringBuilder();
            savaBuilder.append("insert into catalog_version_")
                .append(info.getCompanyId())
                .append("(file_id,url,name,size,suffix,midf_by,midf_time) values (")
                .append(currId)
                .append(",'")
                .append(key.toString())
                .append("','")
                .append(fileNameNew + "',")
                .append(size)
                .append(",'")
                .append(postfix)
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int sum = DAOUtil.executeUpdate(savaBuilder.toString());
            if (sum <= 0)
            {
                return false;
            }
            log.debug("插入历史版本成功=====================================");
            PutObjectResult result = client.putObject(Constant.FLIE_LIBRARY_NAME, key.toString(), content, meta);
            log.debug("oss链接进入上传成功=====================================");
            log.info(result.getETag());
            content.close();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return true;
    }
    
    public boolean uploadFileVersionFile(InputStream content, String id, String url, String fileNameNew, long size, String token, String type)
    {
        try
        {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(token);
            String postfix = fileNameNew.substring(fileNameNew.lastIndexOf("."));
            if (Integer.parseInt(type) == 1)
            {
                key.append(url).append(System.currentTimeMillis()).append(postfix);
            }
            else if (Integer.parseInt(type) == 3)
            {
                if (StringUtils.isBlank(id))
                {
                    Long logId = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
                    logId = logId + 1;
                    id = null;
                    key.append(Constant.PERSONAL_NAME + logId).append("/").append(System.currentTimeMillis()).append(postfix);
                }
                else
                {
                    key.append(url).append(System.currentTimeMillis()).append(postfix);
                }
            }
            // 上传版本记录
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into catalog_version_")
                .append(info.getCompanyId())
                .append("(file_id,url,name,size,suffix,midf_by,midf_time) values (")
                .append(id)
                .append(",'")
                .append(key.toString())
                .append("','")
                .append(fileNameNew)
                .append("',")
                .append(size)
                .append(",'")
                .append(postfix)
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int sum = DAOUtil.executeUpdate(insertBuilder.toString());
            if (sum <= 0)
            {
                return false;
            }
            // 主表记录更新为最新版本
            StringBuilder savaBuilder = new StringBuilder();
            savaBuilder.append("update catalog_")
                .append(info.getCompanyId())
                .append(" set name = '")
                .append(fileNameNew)
                .append("',url ='")
                .append(key.toString())
                .append("',siffix='")
                .append(postfix)
                .append("',size= ")
                .append(size)
                .append(",create_time=")
                .append(System.currentTimeMillis())
                .append(" where id = ")
                .append(id);
            int count = DAOUtil.executeUpdate(savaBuilder.toString());
            if (count <= 0)
            {
                return false;
            }
            PutObjectResult result = client.putObject(Constant.FLIE_LIBRARY_NAME, key.toString(), content, meta);
            log.info(result.getETag());
            content.close();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return true;
    }
    
    public String importFile(InputStream inputStream, String postfix, String contentType, String downloadTem)
    {
        try
        {
            File savedir = new File(downloadTem);
            // 如果目录不存在就创建
            if (!savedir.exists())
            {
                savedir.mkdirs();
            }
            File imageFile = new File(savedir, postfix);
            if (imageFile.exists())
            {
                imageFile.delete();
            }
            FileOutputStream fops = new FileOutputStream(imageFile);
            int length = 0;
            byte[] buf = new byte[1024];
            // 将上传的文件信息保存到相应的文件目录里
            while ((length = inputStream.read(buf)) != -1)
            {
                // 在 buf 数组中 取出数据 写到 （输出流）磁盘上
                fops.write(buf, 0, length);
            }
            fops.close();
            String OS = System.getProperty("os.name").toLowerCase();
            String ffmpegPath = "";
            if (OS.indexOf("linux") >= 0)
            {
                ffmpegPath = downloadTem + "/" + postfix;
            }
            else
            {
                ffmpegPath = downloadTem + "//" + postfix;
            }
            return ffmpegPath;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    public InputStream getletterFile(String buckName, String key, Long width, Long height)
    {
        // 缩放
        StringBuilder style = new StringBuilder();
        if (null == width && null == height)
        {
            OSSObject object = client.getObject(buckName, key);
            return object.getObjectContent();
        }
        else if (null == height)
        {
            style.append("image/resize,w_").append(width);
        }
        else if (null != width && null != height)
        {
            style.append("image/resize,m_fixed,w_").append(width).append(",h_").append(height);
        }
        GetObjectRequest request = new GetObjectRequest(buckName, key);
        request.setProcess(style.toString());
        OSSObject object = client.getObject(request);
        return object.getObjectContent();
    }
    
    public String addFile(HttpServletRequest request, InputStream input, String fileName)
    {
        String pdfName = "";
        try
        {
            String strDirPath = "";
            String OS = System.getProperty("os.name").toLowerCase();
            if (OS.indexOf("linux") >= 0)
            {
                strDirPath = pdfFileLinuxTempUrl + "/";
            }
            else
            {
                strDirPath = pdfFileWindowTempUrl + "//";
            }
            String upFilePath = strDirPath + fileName;
            log.debug("文件预览：fileName = " + fileName + " filePath = " + upFilePath);
            File movieFile = new File(upFilePath);
            FileUtils.copyInputStreamToFile(input, movieFile);
            String stuff = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (stuff.equals("pdf"))
            {
                return upFilePath;
            }
            pdfName = Office2Pdf.getOutputFilePath(upFilePath);
            Office2Pdf.word2pdf(upFilePath);
        }
        catch (IOException e)
        {
            log.debug("文件预览异常：fileName = " + fileName, e);
            e.printStackTrace();
        }
        finally
        {
            try
            {
                if (input != null)
                {
                    
                    input.close();
                }
            }
            catch (IOException e)
            {
                log.debug("文件预览异常：fileName = " + fileName, e);
                e.printStackTrace();
                
            }
        }
        return pdfName;
    }
    
    /**
     * 打印上传文件处理 并返回路径
     * 
     * @param inputStream
     * @param postfix
     * @param contentType
     * @param downloadTem
     * @param bean
     * @param info
     * @return
     * @Description:
     */
    public String printUpload(InputStream inputStream, String postfix, String contentType, String downloadTem, String bean, InfoVo info)
    {
        String url = null;
        try
        {
            File savedir = new File(downloadTem);
            // 如果目录不存在就创建
            if (!savedir.exists())
            {
                savedir.mkdirs();
            }
            
            // 如果文件存在则删除
            File imageFile = new File(savedir, postfix);
            if (imageFile.exists())
            {
                imageFile.delete();
            }
            FileOutputStream fops = new FileOutputStream(imageFile);
            int length = 0;
            byte[] buf = new byte[1024];
            // 将上传的文件信息保存到相应的文件目录里
            while ((length = inputStream.read(buf)) != -1)
            {
                // 在 buf 数组中 取出数据 写到 （输出流）磁盘上
                fops.write(buf, 0, length);
            }
            fops.close();
            /* ExcelUtil. */
            String OS = System.getProperty("os.name").toLowerCase();
            if (OS.indexOf("linux") >= 0)
            {
                url = downloadTem + "/" + postfix;
            }
            else
            {
                url = downloadTem + "//" + postfix;
            }
            commAnalyze(url, bean, info, downloadTem, postfix);
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return url;
    }
    
    /**
     * 解析excel并替换
     * 
     * @param url
     * @param bean
     * @param token
     * @Description:
     */
    private void commAnalyze(String url, String bean, InfoVo info, String downloadTem, String postfix)
    {
        Map<String, String> map = LayoutUtil.jsonParser4Map(info.getCompanyId().toString(), bean, null); // LayoutUtil.
        
        try
        {
            
            FileInputStream fs = new FileInputStream(url);
            Workbook wb = null;
            if (url.toLowerCase().endsWith("xlsx"))
            {
                wb = new XSSFWorkbook(fs);
            }
            else if (url.toLowerCase().endsWith("xls"))
            {
                wb = new HSSFWorkbook(fs);
            }
            Sheet sheet = wb.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            while (rowIterator.hasNext())
            {
                Row row = rowIterator.next();
                Iterator<Cell> cellIterator = row.cellIterator();
                
                if (row != null)
                {
                    // 循环每一列
                    while (cellIterator.hasNext())
                    {
                        Cell cell = cellIterator.next();
                        if (cell != null)
                        {
                            cell.setCellType(HSSFCell.CELL_TYPE_STRING);
                        }
                        if (cell == null || cell.getStringCellValue() == null)
                        {
                            continue;
                        }
                        String value = cell.getStringCellValue();
                        if (!"".equals(value))
                        {
                            
                            // 判断是否存在特殊子表单字符 存在解析
                            if (value.indexOf("*") != -1)
                            {
                                // 获取需要的目标bean
                                String str = value.substring(0, value.indexOf("*"));
                                // 获取需要的目标字段
                                String content = value.substring(value.indexOf("*") + 1, value.length());
                                if (!"".equals(map.get(str)) && !"".equals(map.get(content)))
                                {
                                    cell.setCellValue((String)map.get(str) + "$" + map.get(content));
                                }
                            }
                            else
                            {
                                
                                // 存在相应的转换标识字段
                                if (null != map.get(value) && !"".equals(map.get(value)))
                                {
                                    cell.setCellValue((String)map.get(value));
                                }
                                else
                                {
                                    // 没有转换得标识则原始值
                                    cell.setCellValue(value);
                                }
                            }
                        }
                        else
                        {
                            cell.setCellValue("");
                        }
                        
                    }
                }
            }
            
            File savedir = new File(downloadTem);
            /*
             * if (!savedir.exists()) { savedir.mkdirs(); }
             */
            // 如果文件存在则删除
            File imageFile = new File(savedir, postfix);
            if (imageFile.exists())
            {
                imageFile.delete();
            }
            // 输出文件
            FileOutputStream fileOut = new FileOutputStream(url);
            wb.write(fileOut);
            fileOut.close();
            
        }
        catch (Exception e)
        {
            log.debug(" commAnalyze  异常 ", e);
            e.printStackTrace();
        }
    }
    
    /**
     * 邮件文件上传OSS
     * 
     * @param bucketName
     * @param content
     * @param bean
     * @param fileName
     * @param size
     * @param contentType
     * @param accountId
     * @return
     * @Description:
     */
    public String addEmailImageFile(String bucketName, InputStream content, String bean, String fileName, long size, String contentType, Long accountId)
    {
        try
        {
            if (bucketName == null || bucketName.isEmpty() || content == null)
            {
                return null;
            }
            if (!client.doesBucketExist(bucketName))
            {
                client.createBucket(bucketName);
                client.setBucketAcl(bucketName, CannedAccessControlList.Private);
            }
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            key.append(accountId).append("/").append(bean).append("/").append(System.currentTimeMillis()).append(".").append(fileName);
            PutObjectResult result = client.putObject(bucketName, key.toString(), content, meta);
            
            content.close();
            log.info(result.getETag());
            return emailFileUrl + "bean=" + bean + "&fileName=" + key;
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
    /**
     * 项目文件上传
     * 
     * @param inputStream
     * @param fileNameNew
     * @param originalFilename
     * @param size
     * @param contentType
     * @param parent_id
     * @param token
     * @param data_id
     * @return
     * @Description:
     */
    public String addProjectFile(InputStream inputStream, String fileNameNew, String originalFilename, long size, String contentType, String parent_id, String token,
        String data_id, String type)
    {
        try
        {
            ObjectMetadata meta = new ObjectMetadata();
            meta.setContentLength(size);
            StringBuilder key = new StringBuilder();
            InfoVo info = TokenMgr.obtainInfo(token);
            
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            StringBuilder queryBilder = new StringBuilder();
            // 获取上级目录
            queryBilder.append("select * from  ").append(libraryTable).append(" where data_id = ").append(parent_id).append(" and project_type = ").append(Constant.CURRENCY_TWO);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBilder.toString());
            String suffix = fileNameNew.substring(fileNameNew.lastIndexOf("."));
            key.append(jsonObject.getString("url")).append(System.currentTimeMillis()).append(suffix);
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into ")
                .append(libraryTable)
                .append("(project_type,data_id,file_name,size,url,parent_id,suffix,type,create_by,create_time) values(")
                .append(Constant.CURRENCY_THREE)
                .append(",")
                .append(data_id)
                .append(",'")
                .append(fileNameNew)
                .append("',")
                .append(size)
                .append(",'")
                .append(key)
                .append("',")
                .append(parent_id)
                .append(",'")
                .append(suffix)
                .append("','")
                .append(type)
                .append("',")
                .append(info.getEmployeeId())
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            DAOUtil.executeUpdate(insertBuilder.toString());
            PutObjectResult result = client.putObject(Constant.FLIE_LIBRARY_NAME, key.toString(), inputStream, meta);
            log.info(result.getETag());
            inputStream.close();
            return fileUrl + "fileName=" + key.toString();
        }
        catch (Exception e)
        {
            log.error(e.toString(), e);
        }
        return null;
    }
    
}
