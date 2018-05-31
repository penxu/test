package com.teamface.common.util.dao;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.StringUtil;

/**
 * @Description:
 * @author: dsl
 * @date: 2018年1月4日 下午2:49:23
 * @version: 1.0
 */

public class LibraryDownloadPatchFilesUtil
{
    private static final Logger log = LogManager.getLogger(LibraryDownloadPatchFilesUtil.class);
    
    private static LibraryDownloadPatchFilesUtil instance = null;
    
    private StringBuilder rootDirSB;
    
    private String rootPath;
    
    private String downloadTem;
    
    private String downloadType;
    
    public static synchronized LibraryDownloadPatchFilesUtil getInstance()
    {
        if (instance == null)
        {
            instance = new LibraryDownloadPatchFilesUtil();
        }
        return instance;
    }
    
    private LibraryDownloadPatchFilesUtil()
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        downloadTem = config.getString("teamface.download.tem");
        downloadType = config.getString("teamface.download.type");
    }
    
    /**
     * 
     * @param companyId
     * @param id
     * @return
     * @Description:获取压缩文件
     */
    public String getAllCompressedFiles(Long companyId, Long id)
    {
        // 1.获取下载的目录并新建文件夹
        createRootDir(companyId, id);
        // 2.获取下载目录下面的文件和文件夹，如果是文件则写入，如果是文件夹则继续执行文件夹操作
        createChildDir(companyId, id, rootDirSB);
        // 3.所有文件和文件执行结束后，执行压缩功能
        compressFile();
        return rootDirSB.append(".").append(downloadType).toString();
    }
    
    /**
     * 
     * @param companyId
     * @param id
     * @Description:创建下载目录的文件夹
     */
    public void createRootDir(Long companyId, Long id)
    {
        // 1.1获取下载的目录
        StringBuilder queryRootURL = new StringBuilder().append("select * from catalog_").append(companyId).append(" where id = ").append(id);
        JSONObject rootJson = DAOUtil.executeQuery4FirstJSON(queryRootURL.toString());
        if (null == rootJson)
        {
            log.warn("There is no record of downloading file......");
            return;
        }
        String name = rootJson.getString("name");
        rootPath = null == rootJson ? "" : rootJson.getString("url");
        Matcher m = Pattern.compile("/[a-z0-9]*(/$)").matcher(rootPath);
        if (m.find())
        {
            File teamfaceFile = new File(downloadTem);
            if (!teamfaceFile.exists())
            {
                teamfaceFile.mkdir();
            }
            rootDirSB = new StringBuilder().append(downloadTem).append("/").append(name);
        }
        // 1.2创建下载的目录
        if (!StringUtil.isEmpty(rootDirSB))
        {
            Long currentTime = System.currentTimeMillis();
            rootDirSB = rootDirSB.append("-").append(currentTime);
            File rootFile = new File(rootDirSB.toString());
            if (!rootFile.exists())
            {
                rootFile.mkdir();
            }
        }
    }
    
    /**
     * 
     * @param companyId
     * @param id
     * @param dir
     * @param filePathSB
     * @Description:创建下载目录下面的子目录和文件
     */
    public void createChildDir(Long companyId, Long id, StringBuilder fileDirSB)
    {
        // 2.1获取下载的目录的所有子集
        StringBuilder queryDirChildren = new StringBuilder().append("select * from catalog_").append(companyId).append(" where parent_id = ").append(id);
        List<JSONObject> childrenJsons = DAOUtil.executeQuery4JSON(queryDirChildren.toString());
        if (childrenJsons.size() > 0)
        {
            for (JSONObject child : childrenJsons)
            {
                String childPath = child.getString("url");
                Long childId = child.getLongValue("id");
                String name = child.getString("name");
                if (childPath.endsWith("/"))
                {// 如果是文件夹就创建文件夹
                    Matcher m = Pattern.compile("/[a-z0-9]*(/$)").matcher(childPath);
                    StringBuilder childDir = new StringBuilder(fileDirSB);
                    if (m.find())
                    {
                        childDir = childDir.append("/").append(name);
                    }
                    // 2.2创建下载的目录
                    if (!StringUtil.isEmpty(childDir))
                    {
                        File rootFile = new File(childDir.toString());
                        if (!rootFile.exists())
                        {
                            rootFile.mkdir();
                        }
                        // 2.3 进行文件下面的文件夹和文件的操作
                        createChildDir(companyId, childId, childDir);
                    }
                }
                else
                {// 如果是文件则下载文件
                    StringBuilder temDir = new StringBuilder(fileDirSB).append("/").append(name);
                    OSSUtil.getInstance().getFile(Constant.FLIE_LIBRARY_NAME, childPath, temDir.toString());
                }
                
            }
        }
        
    }
    
    /**
     * 
     * @Description:压缩文件
     */
    public void compressFile()
    {
        // 创建zip输出流
        ZipOutputStream out;
        try
        {
            StringBuilder zipFileName = new StringBuilder().append(rootDirSB).append(".").append(downloadType);
            out = new ZipOutputStream(new FileOutputStream(zipFileName.toString()));
            // 创建缓冲输出流
            File sourceFile = new File(rootDirSB.toString());
            // 调用函数
            compress(out, sourceFile, sourceFile.getName());
            out.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage());
        }
    }
    
    public void compress(ZipOutputStream out, File sourceFile, String base)
        throws Exception
    {
        // 如果路径为目录（文件夹）
        if (sourceFile.isDirectory())
        { // 取出文件夹中的文件（或子文件夹）
            File[] flist = sourceFile.listFiles();
            if (flist.length == 0)
            {// 如果文件夹为空，则只需在目的地zip文件中写入一个目录进入点
                StringBuilder fileDirSB = new StringBuilder().append(base).append("/");
                out.putNextEntry(new ZipEntry(fileDirSB.toString()));
            }
            else
            {// 如果文件夹不为空，则递归调用compress，文件夹中的每一个文件（或文件夹）进行压缩
                for (int i = 0; i < flist.length; i++)
                {
                    StringBuilder baseDirSB = new StringBuilder().append(base).append("/").append(flist[i].getName());
                    compress(out, flist[i], baseDirSB.toString());
                }
            }
        }
        else
        {// 如果不是目录（文件夹），即为文件，则先写入目录进入点，之后将文件写入zip文件中
            out.putNextEntry(new ZipEntry(base));
            FileInputStream fos = new FileInputStream(sourceFile);
            BufferedInputStream bis = new BufferedInputStream(fos);
            int tag = 0;
            // 将源文件写入到zip文件中
            byte[] buffer = new byte[1024];
            while ((tag = bis.read(buffer)) != -1)
            {
                out.write(buffer, 0, tag);
                out.flush();
            }
            bis.close();
            fos.close();
        }
    }
}
