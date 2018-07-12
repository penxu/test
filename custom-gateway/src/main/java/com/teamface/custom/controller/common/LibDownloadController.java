package com.teamface.custom.controller.common;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.Configuration;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.OSSUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.library.FileLibraryAppService;

/**
 * 
 * @Title:
 * @Description:
 * @Author:dsl
 * @Since:2018年1月24日
 * @Version:1.1.0
 */
@Controller
public class LibDownloadController
{
    private static final Logger log = LogManager.getLogger(LibDownloadController.class);
    
    private InputStream is = null;
    
    private BufferedInputStream fos = null;
    
    private OutputStream os = null;
    
    private String fileDir;
    
    @Autowired
    FileLibraryAppService fileLibraryAppService;
    
    private ByteArrayOutputStream outputStream = null;
    
    {
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        fileDir = config.getString("teamface.download.tem");
    }
    
    @RequestMapping(value = "/library/file/downloadCompressedPicture", method = RequestMethod.GET)
    public void downloadCompressedPicture(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long id,
        @RequestParam(required = true) Long width, @RequestParam(required = false) Integer type, @RequestParam(required = false) Long height,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
        {
            return;
        }
        if (null == type)
        {
            type = 1;
        }
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        try
        {
            String queryFileSql = getLibDownloadFileSql(companyId, id, type);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSql);
            if (null == fileJson)
            {
                return;
            }
            String fileUrl = fileJson.getString("url");
            String fileName = fileJson.getString("name");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            is = OSSUtil.getInstance().getCompressedFile(Constant.FLIE_LIBRARY_NAME, fileUrl, width, height);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            byte[] byteData = getInputStreamByte();
            boolean resultStatus = checkOSSReturnContent(byteData);
            if (!resultStatus)
            {
                is = OSSUtil.getInstance().getFile(Constant.FLIE_LIBRARY_NAME, fileUrl);
                fos = new BufferedInputStream(is);
                byte[] byteFullData = getInputStreamByte();
                response.setContentLength(byteFullData.length);
                int count;
                byte[] buffer = new byte[2048];
                while ((count = fos.read(buffer)) > 0)
                {
                    os.write(buffer, 0, count);
                }
            }
            else
            {
                response.setContentLength(byteData.length);
                os.write(byteData);
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeConnection();
        }
    }
    
    /**
     * 
     * @param request 请求信息
     * @param response 返回信息
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param tokenPara 请求参数
     * @param tokenHeader 请求头参数
     * @Description: 下载邮件附件
     */
    @RequestMapping(value = "/email/file/downloadEmailFile", method = RequestMethod.GET)
    public void downloadEmailFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) String fileName,
        @RequestParam(required = true) String fileSize, @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
        {
            return;
        }
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        try
        {
            response.setCharacterEncoding("UTF-8");
            is = new FileInputStream(
                new File(new StringBuilder().append(fileDir).append("//").append(companyId).append("//").append(employeeId).append("//").append(fileName).toString()));
            response.setContentLength(is.available());
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeConnection();
        }
    }
    
    /**
     * 
     * @param request 请求信息
     * @param response 返回信息
     * @param fileName 文件名
     * @param fileSize 文件大小
     * @param tokenPara 请求参数
     * @param tokenHeader 请求头参数
     * @Description: 下载邮件附件
     */
    @RequestMapping(value = "/email/file/downloadThirdEmailFile", method = RequestMethod.GET)
    public void downloadEmailFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) String fileName,
        @RequestParam(required = true) String fileSize, @RequestParam(required = true) Long companyId, @RequestParam(required = true) String employeeId)
    {
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            is = new FileInputStream(
                new File(new StringBuilder().append(fileDir).append("//").append(companyId).append("//").append(employeeId).append("//").append(fileName).toString()));
            response.setContentLength(is.available());
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeConnection();
        }
    }
    
    /**
     * 
     * @param request 请求信息
     * @param response 相应信息
     * @param id 下载文件id
     * @param tokenPara 请求参数
     * @param tokenHeader 请求头参数
     * @Description:下载文件不记录下载次数
     */
    @RequestMapping(value = "/library/file/downloadWithoutRecord", method = RequestMethod.GET)
    public JSONObject downloadWithoutRecord(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long id,
        @RequestParam(required = false) Integer type, @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
        {
            return JsonResUtil.getFailJsonObject();
        }
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        try
        {
            String queryFileSql = getLibDownloadFileSql(companyId, id, type);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSql);
            if (null == fileJson)
            {
                return JsonResUtil.getFailJsonObject();
            }
            String fileUrl = fileJson.getString("url");
            String fileName = fileJson.getString("name");
            Long size = fileJson.getLongValue("size");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
            response.setContentLength(size.intValue());
            is = OSSUtil.getInstance().getFile(Constant.FLIE_LIBRARY_NAME, fileUrl);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        finally
        {
            closeConnection();
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 
     * @param byteData
     * @return
     * @Description:验证阿里云下载缩略图的正确性
     */
    private boolean checkOSSReturnContent(byte[] byteData)
    {
        String result = new String(byteData);
        try
        {
            Document document = DocumentHelper.parseText(result);
            if (document.getRootElement().getQName().getName().equals("Error"))
            {
                return false;
            }
        }
        catch (DocumentException e)
        {
            return true;
        }
        return true;
    }
    
    private void closeConnection()
    {
        try
        {
            if (is != null)
            {
                
                is.close();
            }
            if (fos != null)
            {
                
                fos.close();
            }
            if (os != null)
            {
                
                os.close();
            }
            
            if (outputStream != null)
            {
                
                outputStream.close();
            }
        }
        catch (IOException e)
        {
            
            e.printStackTrace();
            
        }
    }
    
    private byte[] getInputStreamByte()
        throws IOException
    {
        byte[] byteBuffer = new byte[1024];
        int len = -1;
        outputStream = new ByteArrayOutputStream();
        while ((len = is.read(byteBuffer)) != -1)
        {
            outputStream.write(byteBuffer, 0, len);
        }
        return outputStream.toByteArray();
    }
    
    /**
     * 
     * @param companyId
     * @param id
     * @param type
     * @return
     * @Description:根据下载的类型获取下载文件的SQL
     */
    private String getLibDownloadFileSql(Long companyId, Long id, Integer type)
    {
        type = type == null ? 1 : type;
        StringBuilder queryFileSqlSB = new StringBuilder();
        if (type == 1)
        {
            queryFileSqlSB.append("select * from catalog_").append(companyId).append(" where id = ").append(id);
        }
        if (type == 2)
        {
            queryFileSqlSB.append("select * from catalog_version_").append(companyId).append(" where id = ").append(id);
        }
        return queryFileSqlSB.toString();
    }
}
