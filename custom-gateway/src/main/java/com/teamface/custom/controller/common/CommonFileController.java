package com.teamface.custom.controller.common;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.cache.ServiceResultCodeCache;
import com.teamface.common.constant.Constant;
import com.teamface.common.constant.DataTypes;
import com.teamface.common.constant.FileBackListErum;
import com.teamface.common.exception.ServiceException;
import com.teamface.common.file.FileUtil;
import com.teamface.common.msg.util.ParseTxtFromFile;
import com.teamface.common.util.ExcelUtil;
import com.teamface.common.util.FfmpegUtil;
import com.teamface.common.util.FileDaoUtil;
import com.teamface.common.util.JsonResUtil;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.StringUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.dao.LibraryDownloadPatchFilesUtil;
import com.teamface.common.util.dao.OSSUtil;
import com.teamface.common.util.dao.Office2Pdf;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.employee.EmployeeAppService;
import com.teamface.custom.service.library.FileLibraryAppService;
import com.teamface.custom.service.module.ModuleAppService;

/**
 * @Description:
 * @author: mofan
 * @date: 2017年11月14日 上午11:58:13
 * @version: 1.0
 */
@Controller
public class CommonFileController
{
    private static final Logger LOG = LogManager.getLogger(CommonFileController.class);
    
    private static ServiceResultCodeCache resultCode = ServiceResultCodeCache.getInstance();
    
    @Autowired
    private EmployeeAppService employeeAppService;
    
    @Autowired
    private ModuleAppService moduleAppService;
    
    @Autowired
    private FileLibraryAppService fileLibraryAppService;
    
    PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
    
    Configuration config = properties.getConfig();
    
    String ffmpegUrl = config.getString("ffmpeg.linux.url");
    
    String fileUrl = config.getString("teamface.file.url");
    
    String pdfFileWindowTempUrl = config.getString("teamface.download.window.pdfdir");
    
    String pdfFileLinuxTempUrl = config.getString("teamface.download.linux.pdfdir");
    
    String vedioFileUrl = config.getString("teamface.openmovie.url");
    
    String imageFileUrl = config.getString("teamface.imagefile.url");
    
    /**
     * 公司初始化头像上传
     * 
     * @param request
     * @param bean
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/imageUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject imageUpload(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long accountId = info.getAccountId();
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            resultArray = imageUploadFile("company", fileList, accountId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    /**
     * 文件上传
     * 
     * @param request
     * @param bean
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/upload", method = RequestMethod.POST)
    public @ResponseBody JSONObject fileUpload(HttpServletRequest request, @RequestParam(value = "bean", required = true) String bean,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            resultArray = uploadFile(bean, fileList, companyId, employeeId, request);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    /**
     * 应用模块文件上传
     * 
     * @param request
     * @param bean
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/applyFileUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject applyFileUpload(HttpServletRequest request, @RequestParam(value = "bean", required = true) String bean,
        @RequestParam(value = "data_id", required = false) String data_id, @RequestParam(value = "file_size", required = false) String file_size,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
                if (!"".equals(file_size) && null != file_size)
                {
                    if (file.getSize() > Long.valueOf(file_size))
                    {
                        return JsonResUtil.getResultJsonByIdent("common.upload.file.size.error");
                    }
                }
            }
            resultArray = uploadApplyFile(bean, fileList, token, data_id);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    /**
     * 文件库上传
     * 
     * @param request
     * @param url
     * @param token
     * @return id
     * @Description:
     */
    @RequestMapping(value = "/common/file/fileLibraryUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject FileLibraryUpload(HttpServletRequest request, @RequestParam(value = "url", required = true) String url,
        @RequestParam(value = "id", required = true) String id, @RequestParam(value = "style", required = true) String style,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        LOG.debug("进入上传方法=====================================");
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            if (Integer.parseInt(style) == Constant.CURRENCY_ONE)
            {
                boolean flag = fileLibraryAppService.vailFileUploadAuth(token, Long.valueOf(id), style);
                if (!flag)
                {
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.auth.error");
                }
            }
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            boolean falg = uploadFileLibraryFile(url, fileList, token, id, style);
            if (!falg)
            {
                return JsonResUtil.getFailJsonObject();
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    /**
     * 文件库上传版本
     * 
     * @param request
     * @param url
     * @param token
     * @return id
     * @Description:
     */
    @RequestMapping(value = "/common/file/fileVersionUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject fileVersionUpload(HttpServletRequest request, @RequestParam(value = "url", required = true) String url,
        @RequestParam(value = "id", required = true) String id, @RequestParam(value = "style", required = true) String style,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            boolean falg = uploadFileVersionFile(url, fileList, token, id, style);
            if (!falg)
            {
                return JsonResUtil.getFailJsonObject();
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    private boolean uploadFileVersionFile(String url, List<MultipartFile> fileList, String token, String id, String type)
    {
        boolean falge = true;
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("originalFileName", fileNameNew);
                attachmentJson.put("fileName", fileNameNew);
                attachmentJson.put("fileType", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("fileSize", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serialNumber", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                String fileUrlPath = FileDaoUtil.uploadFileVersionFile(id, url, fileNameNew, file.getSize(), token, type);
                if (StringUtils.isEmpty(fileUrlPath))
                {
                    return false;
                }
                falge = OSSUtil.getInstance().addFile(Constant.FLIE_LIBRARY_NAME, file.getInputStream(), file.getSize(), fileUrlPath);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            try
            {
                throw new ServiceException(e);
            }
            catch (ServiceException e1)
            {
                e1.printStackTrace();
            }
        }
        return falge;
    }
    
    /**
     * 文件库处理
     * 
     * @param url
     * @param fileList
     * @param token
     * @param id
     * @param type
     * @return
     */
    private boolean uploadFileLibraryFile(String url, List<MultipartFile> fileList, String token, String id, String type)
    {
        LOG.debug("进入上传方法处理=====================================");
        boolean falge = true;
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                LOG.debug("oss链接=====================================");
                StringBuilder ids = new StringBuilder();
                String fileUrlPath = FileDaoUtil.savaFileLibraryFile(id, url, fileNameNew, file.getSize(), token, type, ids);
                if (StringUtils.isEmpty(fileUrlPath))
                {
                    return false;
                }
                
                falge = OSSUtil.getInstance().addFile(Constant.FLIE_LIBRARY_NAME, file.getInputStream(), file.getSize(), fileUrlPath);
                
                if (falge)
                {
                    // .txt .pdf .xls xlsx docx doc dot pptx ppt
                    if (postfix.equals("txt") || postfix.equals("pdf") || postfix.equals("xls") || postfix.equals("xlsx") || postfix.equals("docx") || postfix.equals("doc")
                        || postfix.equals("dot") || postfix.equals("pptx") || postfix.equals("ppt"))
                    {
                        
                        String OS = System.getProperty("os.name").toLowerCase();
                        String strDirPath = "";
                        if (OS.indexOf("linux") >= 0)
                        {
                            strDirPath = pdfFileLinuxTempUrl + "/";
                        }
                        else
                        {
                            strDirPath = pdfFileWindowTempUrl + "//";
                        }
                        String upFilePath = strDirPath + fileUrlPath;
                        LOG.debug(" 转为pdf文件：fileName = " + fileUrlPath + " filePath = " + upFilePath);
                        File movieFile = new File(upFilePath);
                        FileUtils.copyInputStreamToFile(file.getInputStream(), movieFile);
                        String pdfName = Office2Pdf.getOutputFilePath(fileUrlPath);
                        Office2Pdf.word2pdf(upFilePath);
                        OSSUtil.getInstance().addFile(Constant.FLIE_LIBRARY_NAME, movieFile);
                        if (falge)
                        {
                            
                            FileDaoUtil.updatePDFUrlByTransfor(ids.toString(), pdfName, token);
                            deleteDir(new File(strDirPath));
                        }
                    }
                    
                }
            }
        }
        catch (Exception e)
        {
            LOG.error(" 文件库文件上传 异常  " + e.getMessage(), e);
            try
            {
                throw new ServiceException(e);
            }
            catch (ServiceException e1)
            {
                e1.printStackTrace();
            }
        }
        return falge;
    }
    
    /**
     * 
     * @param dir
     * @return
     * @Description:删除目录下所有文件
     */
    private boolean deleteDir(File dir)
    {
        if (dir.isDirectory())
        {
            String[] children = dir.list();// 递归删除目录中的子目录下
            for (int i = 0; i < children.length; i++)
            {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success)
                {
                    return false;
                }
            }
        }
        return dir.delete();
    }
    
    private JSONArray uploadApplyFile(String beanName, List<MultipartFile> fileList, String token, String data_id)
        throws ServiceException
    {
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                InfoVo info = TokenMgr.obtainInfo(token);
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                JSONObject employee = employeeAppService.queryEmployee(info.getEmployeeId(), info.getCompanyId());
                attachmentJson.put("upload_by", employee.get("employee_name") == null ? "" : employee.get("employee_name"));
                attachmentJson.put("upload_time", System.currentTimeMillis());
                
                Map<String, Object> map = new HashMap<>();
                map.put("token", token);
                map.put("bean", beanName);
                String applyId = moduleAppService.getModuleBelongWhichApp(map);
                
                String fileUrlPath = FileDaoUtil.addApplyFile(beanName, file.getOriginalFilename(), file.getSize(), applyId, token, data_id);
                if (StringUtils.isEmpty(fileUrlPath))
                {
                    return null;
                }
                
                String fileUrl = OSSUtil.getInstance().addFile(Constant.FLIE_LIBRARY_NAME, file.getInputStream(), fileUrlPath, file.getSize());
                if (!StringUtils.isEmpty(fileUrl))
                {// 有可能会上传失败！
                    String filePathUrl = config.getString("teamface.file.url");
                    filePathUrl = filePathUrl.concat("bean=").concat(beanName).concat("&fileName=").concat(fileUrlPath);
                    attachmentJson.put("file_url", filePathUrl);
                }
                else
                {
                    ServiceException serviceException = new ServiceException(resultCode.get("common.upload.file.interrupt"), resultCode.getMsgZh("common.upload.file.interrupt"));
                    throw serviceException;
                }
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            throw new ServiceException(e);
        }
        return resultArray;
    }
    
    /**
     * 把token当作参数传递过来获取文件
     * 
     * @param request
     * @param response
     * @param fileName
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/download", method = RequestMethod.GET)
    public @ResponseBody void download(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "fileName", required = true) String fileName,
        @RequestParam(value = "definitionFileName", required = false) String definitionFileName,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String token1, @RequestParam(required = false) Long width,
        @RequestParam(value = "contentType", required = false) String contentType, @RequestParam(required = false) Long height,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String token2)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        String buckName = Constant.FLIE_LIBRARY_NAME;
        response.setCharacterEncoding("UTF-8");
        if (!StringUtils.isEmpty(request.getParameter("fileSize")))
        {
            Long size = Long.valueOf(request.getParameter("fileSize"));
            response.setContentLength(size.intValue());
        }
        if (!StringUtils.isEmpty(request.getParameter("contentType")))
        {
            response.setContentType(contentType);
        }
        try
        {
            if (!StringUtils.isEmpty(definitionFileName))
            {
                response.setHeader("content-disposition", "attachment; filename=" + java.net.URLEncoder.encode(definitionFileName, "UTF-8"));
            }
            else
            {
                response.setHeader("content-disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            }
            if (StringUtil.isEmpty(token1) && StringUtil.isEmpty(token2))
            {
                return;
            }
            String token = StringUtil.isEmpty(token1) ? token2 : token1;
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            if (width != null && height != null)
            {
                
                is = OSSUtil.getInstance().getCompressedFile(buckName, fileName, width, height);
            }
            else
            {
                is = OSSUtil.getInstance().getFile(buckName, fileName);
                
            }
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            try
            {
                
                is = OSSUtil.getInstance().getFile(buckName, fileName);
                fos = new BufferedInputStream(is);
                os = response.getOutputStream();
                int count;
                byte[] buffer = new byte[2048];
                while ((count = fos.read(buffer)) > 0)
                {
                    os.write(buffer, 0, count);
                }
                os.flush();
            }
            catch (Exception ae)
            {
                LOG.error(ae.getMessage(), ae);
            }
            finally
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
                }
                catch (IOException ee)
                {
                    LOG.error(" file get again" + ee.getMessage(), ee);
                    ee.printStackTrace();
                    
                }
                
            }
        }
        finally
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
            }
            catch (IOException e)
            {
                LOG.error(" file get first " + e.getMessage(), e);
                
            }
            
        }
    }
    
    /**
     * 把token当作参数获取视频音频文件
     * 
     * @param request
     * @param response
     * @param bean
     * @param fileName
     * @param contentType
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/download4Show", method = RequestMethod.GET)
    public @ResponseBody void download4Show(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "bean", required = true) String bean,
        @RequestParam(value = "fileName", required = true) String fileName, @RequestParam(value = "contentType", required = true) String contentType,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String token1,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String token2)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            if (StringUtil.isEmpty(token1) && StringUtil.isEmpty(token2))
            {
                return;
            }
            String token = StringUtil.isEmpty(token1) ? token2 : token1;
            String buckName = Constant.FLIE_LIBRARY_NAME;
            response.setCharacterEncoding("UTF-8");
            response.setContentType(contentType);
            if (!StringUtils.isEmpty(request.getParameter("fileSize")))
            {
                Long size = Long.valueOf(request.getParameter("fileSize"));
                response.setContentLength(size.intValue());
            }
            is = OSSUtil.getInstance().getFile(buckName, fileName);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
    }
    
    /**
     * 把token当作参数传递过来获取公司头像文件
     * 
     * @param request
     * @param response
     * @param fileName
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/imageDownload", method = RequestMethod.GET)
    public @ResponseBody void imageDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "fileName", required = true) String fileName,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String token1,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String token2)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            if (StringUtil.isEmpty(token1) && StringUtil.isEmpty(token2))
            {
                return;
            }
            String buckName = Constant.COMPANY_LIBRARY_NAME;
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            is = OSSUtil.getInstance().getFile(buckName, fileName);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
    }
    
    private JSONArray uploadFile(String beanName, List<MultipartFile> fileList, Long companyId, Long employeeId, HttpServletRequest request)
        throws ServiceException
    {
        String buckName = Constant.FLIE_LIBRARY_NAME;
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String contentType = file.getContentType();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                long fileSize = file.getSize();
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", fileSize);// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                JSONObject employee = employeeAppService.queryEmployee(employeeId, companyId);
                attachmentJson.put("upload_by", employee.get("employee_name") == null ? "" : employee.get("employee_name"));
                attachmentJson.put("upload_time", System.currentTimeMillis());
                InputStream content = file.getInputStream();
                InputStream inputContent = file.getInputStream();
                String keyString = OSSUtil.getInstance().addFile(buckName, content, fileNameNew, fileSize);
                
                if (!StringUtils.isEmpty(contentType) && (contentType.startsWith("audio") || contentType.startsWith("video")))
                {
                    String url = vedioFileUrl + "bean=" + beanName + "&fileName=" + keyString + "&contentType=" + contentType + "&fileSize=" + fileSize;
                    attachmentJson.put("file_url", url);
                    // String strDirPath =
                    // request.getSession().getServletContext().getRealPath("/");
                    String strDirPath = CommonFileController.class.getResource("/").getPath();
                    String upFilePath = strDirPath + File.separator + keyString;
                    File movieFile = new File(upFilePath);
                    FileUtils.copyInputStreamToFile(inputContent, movieFile);
                    String imageMat = "png";
                    keyString = keyString.substring(0, keyString.lastIndexOf("."));
                    keyString += "." + imageMat;
                    String mediaPicPath = strDirPath + File.separator + keyString;
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
                    // 截图配置调用
                    FfmpegUtil.process(ffmpegPath, upFilePath, mediaPicPath);
                    File picfile = new File(mediaPicPath);
                    String thum_url = "";
                    String thum_key = "";
                    if (picfile.exists())
                    {
                        thum_key = OSSUtil.getInstance().addFile(buckName, picfile);
                        thum_url = fileUrl + "bean=" + beanName + "&fileName=" + thum_key + "&fileSize=" + movieFile.length();
                        picfile.delete();
                    }
                    else
                    {
                        picfile.createNewFile();
                        thum_key = OSSUtil.getInstance().addFile(buckName, picfile);
                        thum_url = fileUrl + "bean=" + beanName + "&fileName=" + thum_key + "&fileSize=" + movieFile.length();
                        picfile.delete();
                    }
                    File upFile = new File(upFilePath);
                    if (upFile.exists())
                    {
                        upFile.delete();
                    }
                    attachmentJson.put("video_thumbnail_url", thum_url);
                    
                }
                else
                {
                    String url = fileUrl + "bean=" + beanName + "&fileName=" + keyString + "&fileSize=" + fileSize;
                    attachmentJson.put("file_url", url);
                }
                resultArray.add(attachmentJson);
            }
        }
        catch (
        
        Exception e)
        
        {
            throw new ServiceException(e);
        }
        return resultArray;
        
    }
    
    private JSONArray imageUploadFile(String beanName, List<MultipartFile> fileList, Long accountId)
        throws ServiceException
    {
        String buckName = Constant.COMPANY_LIBRARY_NAME;
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                String key = OSSUtil.getInstance().addFile(buckName, file.getInputStream(), fileNameNew, file.getSize());
                String fileUrl = imageFileUrl + "bean=" + beanName + "&fileName=" + key + "&fileSize=" + file.getSize();
                attachmentJson.put("file_url", fileUrl);
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException(e);
        }
        return resultArray;
    }
    
    @RequestMapping(value = "/library/file/batchDownload", method = RequestMethod.GET)
    public JSONObject batchDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long id,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        File downloadFile = null;
        String filePath = "";
        try
        {
            if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
            {
                return JsonResUtil.getFailJsonObject();
            }
            String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            Long employeeId = info.getEmployeeId();
            // 添加文件夹下载权限
            boolean flag = fileLibraryAppService.vailFileAuth(token, id);
            if (!flag)
            {
                return JsonResUtil.getFailJsonObject();
            }
            response.setCharacterEncoding("UTF-8");
            filePath = LibraryDownloadPatchFilesUtil.getInstance().getAllCompressedFiles(companyId, employeeId, id);
            String fileName = filePath.substring(filePath.lastIndexOf("/") + 1);
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName));
            
            downloadFile = new File(filePath);
            Long size = downloadFile.length();
            response.setContentLength(size.intValue());
            is = new FileInputStream(downloadFile);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                if (null != downloadFile && downloadFile.isFile())
                {
                    downloadFile.delete();
                }
            }
            catch (IOException e)
            {
                LOG.error(e.getMessage(), e);
            }
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    @RequestMapping(value = "/library/file/download", method = RequestMethod.GET)
    public JSONObject libraryDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long id,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
        {
            return JsonResUtil.getFailJsonObject();
        }
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        try
        {
            // 添加文件下载权限
            boolean flag = fileLibraryAppService.vailFileLibararyAuth(token, id);
            if (!flag)
            {
                return JsonResUtil.getFailJsonObject();
            }
            StringBuilder queryFileSqlSB = new StringBuilder().append("select * from catalog_").append(companyId).append(" where id = ").append(id);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSqlSB.toString());
            if (null == fileJson)
            {
                return JsonResUtil.getFailJsonObject();
            }
            String fileUrl = fileJson.getString("url");
            String fileName = fileJson.getString("name");
            Long size = fileJson.getLongValue("size");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName));
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
            os.flush();
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                updateDownloadRecord(companyId, employeeId, id);
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    @RequestMapping(value = "/library/file/downloadHistoryFile", method = RequestMethod.GET)
    public JSONObject downloadHistoryFile(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long id,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
        {
            return JsonResUtil.getFailJsonObject();
        }
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        Long employeeId = info.getEmployeeId();
        Long fileId;
        try
        {
            
            StringBuilder queryFileSqlSB = new StringBuilder().append("select * from catalog_version_").append(companyId).append(" where id = ").append(id);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSqlSB.toString());
            if (null == fileJson)
            {
                return JsonResUtil.getFailJsonObject();
            }
            String fileUrl = fileJson.getString("url");
            String fileName = fileJson.getString("name");
            Long size = fileJson.getLongValue("size");
            fileId = fileJson.getLongValue("file_id");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(fileName));
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
            os.flush();
            updateDownloadRecord(companyId, employeeId, fileId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
        return JsonResUtil.getSuccessJsonObject();
    }
    
    synchronized void updateDownloadRecord(Long companyId, Long employeeId, Long id)
    {
        StringBuilder queryDownloadRecordSqlSB =
            new StringBuilder().append("select * from download_record_").append(companyId).append(" where file_id = ").append(id).append(" and employee_id = ").append(employeeId);
        JSONObject recordJson = DAOUtil.executeQuery4FirstJSON(queryDownloadRecordSqlSB.toString());
        if (null == recordJson)
        {
            StringBuilder insertRecordSqlSB = new StringBuilder().append("insert into download_record_")
                .append(companyId)
                .append(" (file_id,employee_id,number,lately_time)")
                .append(" values(")
                .append(id)
                .append(",")
                .append(employeeId)
                .append(",")
                .append(1)
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            DAOUtil.executeUpdate(insertRecordSqlSB.toString());
        }
        else
        {
            Long recordId = recordJson.getLongValue("id");
            Long number = recordJson.getLongValue("number");
            StringBuilder insertRecordSqlSB = new StringBuilder().append("update download_record_")
                .append(companyId)
                .append(" set number = ")
                .append(number + 1)
                .append(",")
                .append("lately_time = ")
                .append(System.currentTimeMillis())
                .append(" where id = ")
                .append(recordId);
            DAOUtil.executeUpdate(insertRecordSqlSB.toString());
        }
    }
    
    /**
     * 导入模版上传
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/importUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject importUpload(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            Long accountId = info.getAccountId();
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            resultArray = importFileUpload("company", fileList, accountId);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
        
    }
    
    private JSONArray importFileUpload(String string, List<MultipartFile> fileList, Long accountId)
    {
        
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        String downloadTem = config.getString("teamface.download.tem");
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String contentType = file.getContentType();
                String postfix = System.currentTimeMillis() + ".xlsx";
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                String fileUrl = FileDaoUtil.importTemplateFile(file.getInputStream(), postfix, contentType, downloadTem);
                if (!StringUtils.isEmpty(fileUrl))
                {// 有可能会上传失败！
                    attachmentJson.put("file_url", fileUrl);
                }
                else
                {
                    ServiceException serviceException = new ServiceException(resultCode.get("common.upload.file.interrupt"), resultCode.getMsgZh("common.upload.file.interrupt"));
                    throw serviceException;
                }
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            try
            {
                throw new ServiceException(e);
            }
            catch (ServiceException e1)
            {
                LOG.error(e.getMessage(), e1);
            }
        }
        return resultArray;
        
    }
    
    /**
     * 根据bean 创建模版并下载
     * 
     * @param request
     * @param response
     * @param bean
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/downloadTemplate", method = RequestMethod.GET)
    public @ResponseBody void downloadTemplate(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "bean", required = true) String bean,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
            InfoVo info = TokenMgr.obtainInfo(token);
            Long companyId = info.getCompanyId();
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + java.net.URLEncoder.encode("模版.xlsx", "UTF-8"));
            JSONObject jsonObject = LayoutUtil.getNotSystemEnableLayout(companyId.toString(), bean, "0");
            PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
            Configuration config = properties.getConfig();
            String downloadTem = config.getString("teamface.download.template");
            int sum = ExcelUtil.createTemplate(info.getCompanyId(), jsonObject, downloadTem + "/模版.xlsx");
            if (sum <= 0)
            {
                
            }
            is = new FileInputStream(downloadTem + "/模版.xlsx");
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            response.setContentLength(is.available());
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
    }
    
    /**
     * 下载错误
     * 
     * @param request
     * @param response
     * @param bean
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/downloadError", method = RequestMethod.GET)
    public @ResponseBody void errorTemplate(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "url", required = true) String url)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + java.net.URLEncoder.encode("错误日志.txt", "UTF-8"));
            is = new FileInputStream(url);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            response.setContentLength(is.available());
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
    }
    
    @RequestMapping(value = "/common/online/preview", method = RequestMethod.POST)
    public @ResponseBody JSONObject preview(HttpServletRequest request, HttpServletResponse response, @RequestBody(required = true) String reqstr,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        JSONObject json = new JSONObject();
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        try
        {
            JSONObject reqJson = JSONObject.parseObject(reqstr);
            StringBuilder queryFileSqlSB = new StringBuilder().append("select * from catalog_").append(companyId).append(" where id = ").append(reqJson.getString("id"));
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSqlSB.toString());
            if (null == fileJson)
            {
                return null;
            }
            String fileUrl = fileJson.getString("pdf_url");
            String fileName = fileJson.getString("name");
            String postfix = fileName.substring(fileName.lastIndexOf(".") + 1);
            if (!postfix.equals("pdf"))
            {
                fileName = fileName.substring(0, fileName.lastIndexOf(".")) + "pdf";
            }
            Long size = fileJson.getLongValue("size");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment; filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
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
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
        return JsonResUtil.getSuccessJsonObject(json);
    }
    
    /**
     * 根据文件路径预览
     * 
     * @param request
     * @param response
     * @param fileUrl
     * @param tokenPara
     * @param tokenHeader
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/online/previewByUrl", method = RequestMethod.GET)
    public @ResponseBody JSONObject previewByUrl(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) String fileUrl,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        JSONObject json = new JSONObject();
        InputStream is = null;
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + fileUrl);
            is = OSSUtil.getInstance().getFile(Constant.FLIE_LIBRARY_NAME, fileUrl);
            String OS = System.getProperty("os.name").toLowerCase();
            String strDirPath = "";
            if (OS.indexOf("linux") >= 0)
            {
                strDirPath = pdfFileLinuxTempUrl + "/";
            }
            else
            {
                strDirPath = pdfFileWindowTempUrl + "//";
            }
            String upFilePath = strDirPath + fileUrl;
            LOG.debug("文件预览：fileName = " + fileUrl + " filePath = " + upFilePath);
            File movieFile = new File(upFilePath);
            FileUtils.copyInputStreamToFile(is, movieFile);
            String stuff = fileUrl.substring(fileUrl.lastIndexOf(".") + 1);
            if (stuff.equals("pdf"))
            {
                json.put("pdfUrl", upFilePath);
                return json;
            }
            String pdfName = Office2Pdf.getOutputFilePath(upFilePath);
            Office2Pdf.word2pdf(upFilePath);
            json.put("pdfUrl", pdfName);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
        {
            try
            {
                if (is != null)
                {
                    
                    is.close();
                }
            }
            catch (IOException e)
            {
                
                e.printStackTrace();
                
            }
            
        }
        return JsonResUtil.getSuccessJsonObject(json);
    }
    
    /**
     * 文件库公开链接地址访问
     * 
     * @param request
     * @param response
     * @param sign
     * @param visit_pwd
     * @return
     * @Description:
     */
    @RequestMapping(value = "/library/file/openDownload", method = RequestMethod.GET)
    public @ResponseBody JSONObject openDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) String sign,
        @RequestParam(required = false) String visit_pwd)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            
            String str = ParseTxtFromFile.lBytesToString(Base64.getDecoder().decode(sign), 0, Base64.getDecoder().decode(sign).length, null);
            String companyId = str.substring(str.indexOf("company_id=") + 11, str.indexOf("&type"));
            String id = str.substring(str.indexOf("?id=") + 4, str.indexOf("&company_id"));
            String type = str.substring(str.indexOf("&type=") + 6, str.length());
            StringBuilder querySql = new StringBuilder().append("select * from open_file_url_").append(companyId).append(" where file_id = ").append(id);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(querySql.toString());
            // 验证是否存在公开
            if (null == jsonObject)
            {
                return JsonResUtil.getResultJsonByIdent("postprocess.open.download.time.error");
            }
            if (null != jsonObject.getLong("end_time"))
            {
                // 判断有效时长
                if (System.currentTimeMillis() > jsonObject.getLong("end_time"))
                {
                    return JsonResUtil.getResultJsonByIdent("postprocess.open.download.time.error");
                }
            }
            if ("0".equals(type))
            {
                // 验证访问密码
                if (!"".equals(jsonObject.getString("visit_pwd")) && !"".equals(visit_pwd) && null != visit_pwd)
                {
                    if (!jsonObject.getString("visit_pwd").equals(visit_pwd))
                    {
                        return JsonResUtil.getResultJsonByIdent("postprocess.open.download.pwd.error");
                    }
                }
                else
                { // 是否需要填写密码
                    if (!"".equals(jsonObject.getString("visit_pwd")) && null != jsonObject.getString("visit_pwd"))
                    {
                        return JsonResUtil.getResultJsonByIdent("postprocess.open.download.error");
                    }
                }
            }
            // 根据ID查询
            StringBuilder queryFileSqlSB = new StringBuilder().append("select * from catalog_").append(companyId).append(" where id = ").append(id);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryFileSqlSB.toString());
            if (null == fileJson)
            {
                return null;
            }
            String fileUrl = fileJson.getString("url");
            String fileName = fileJson.getString("name");
            Long size = fileJson.getLongValue("size");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + fileName);
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
            os.flush();
            
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
            }
            catch (IOException e)
            {
                
                LOG.error(e.getMessage(), e);
                
            }
            
        }
        return JsonResUtil.getResultJsonByIdent("common.sucess");
    }
    
    /**
     * 打印上传文件保存
     * 
     * @param request
     * @param bean
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/printUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject printUpload(HttpServletRequest request, @RequestParam(value = "bean", required = true) String bean,
        @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
                if (!postfix.contains("xls"))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            resultArray = printUpload(bean, fileList, token, request);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    private JSONArray printUpload(String bean, List<MultipartFile> fileList, String token, HttpServletRequest request)
    {
        
        InfoVo info = TokenMgr.obtainInfo(token);
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        String downloadTem = config.getString("teamface.print.template");
        downloadTem += "/" + info.getCompanyId() + "/" + bean + "/printTemplate";
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = System.currentTimeMillis() + ".xls";
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                
                String fileUrl = FileDaoUtil.printUpload(file.getInputStream(), postfix, downloadTem, bean, info);
                if (!StringUtils.isEmpty(fileUrl))
                {// 有可能会上传失败！
                    attachmentJson.put("file_url", fileUrl);
                }
                else
                {
                    ServiceException serviceException = new ServiceException(resultCode.get("common.upload.file.interrupt"), resultCode.getMsgZh("common.upload.file.interrupt"));
                    throw serviceException;
                }
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            try
            {
                throw new ServiceException(e);
            }
            catch (ServiceException e1)
            {
                e1.printStackTrace();
            }
        }
        return resultArray;
    }
    
    /**
     * 下载打印数据文件
     * 
     * @param request
     * @param response
     * @param bean
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/printDataDownload", method = RequestMethod.GET)
    public @ResponseBody void printDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "id", required = true) String id,
        @RequestParam(value = "bean", required = true) String bean, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + java.net.URLEncoder.encode("打印模版.xls", "UTF-8"));
            InfoVo info = TokenMgr.obtainInfo(token);
            StringBuilder str = new StringBuilder();
            String printTable = DAOUtil.getTableName("bean_print_url", info.getCompanyId());
            str.append("select print_url from ").append(printTable).append(" where  id =  ").append(id).append(" and bean = '" + bean + "'");
            Object object = DAOUtil.executeQuery4Object(str.toString());
            is = new FileInputStream(object.toString());
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            response.setContentLength(is.available());
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
            }
            catch (IOException e)
            {
                e.printStackTrace();
                
            }
            
        }
    }
    
    /**
     * 下载打印模版文件
     * 
     * @param request
     * @param response
     * @param bean
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/printTemplateDownload", method = RequestMethod.GET)
    public @ResponseBody void printTemplateDownload(HttpServletRequest request, HttpServletResponse response)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            String path = Constant.PRINT_TEMPLATE_DOWNLOAD;
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + java.net.URLEncoder.encode("打印模版.xls", "UTF-8"));
            is = new FileInputStream(path);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            response.setContentLength(is.available());
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
            }
            catch (IOException e)
            {
                e.printStackTrace();
                
            }
            
        }
    }
    
    /**
     * 邮件图片文件上传
     * 
     * @param request
     * @param bean
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/emailImageUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject emailImageUpload(HttpServletRequest request)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            resultArray = emailImageUploadFile("email", fileList, 1L);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    /**
     * 解析处理邮件上传文件
     * 
     * @param beanName
     * @param fileList
     * @param accountId
     * @return
     * @Description:
     */
    private JSONArray emailImageUploadFile(String beanName, List<MultipartFile> fileList, Long accountId)
    {
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                StringBuilder key = new StringBuilder();
                key.append(accountId).append("/").append(beanName).append("/").append(System.currentTimeMillis()).append(".").append(fileNameNew);
                
                String fileUrl = OSSUtil.getInstance().addFile(Constant.COMPANY_LIBRARY_NAME, file.getInputStream(), key.toString(), file.getSize());
                if (!StringUtils.isEmpty(fileUrl))
                {// 有可能会上传失败！
                    
                    String pathUrl = config.getString("teamface.emailfile.url");
                    pathUrl.concat("bean=").concat(beanName).concat("&fileName=").concat(key.toString());
                    attachmentJson.put("file_url", pathUrl);
                }
                else
                {
                    ServiceException serviceException = new ServiceException(resultCode.get("common.upload.file.interrupt"), resultCode.getMsgZh("common.upload.file.interrupt"));
                    throw serviceException;
                }
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            try
            {
                throw new ServiceException(e);
            }
            catch (ServiceException e1)
            {
                LOG.error(e.getMessage(), e);
            }
        }
        return resultArray;
    }
    
    /**
     * 郵件文件下載
     * 
     * @param request
     * @param response
     * @param fileName
     * @param token
     * @Description:
     */
    @RequestMapping(value = "/common/file/emailFileDownload", method = RequestMethod.GET)
    public @ResponseBody void emailFileDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(value = "fileName", required = true) String fileName)
    {
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            String buckName = Constant.COMPANY_LIBRARY_NAME;
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
            is = OSSUtil.getInstance().getFile(buckName, fileName);
            fos = new BufferedInputStream(is);
            os = response.getOutputStream();
            int count;
            byte[] buffer = new byte[2048];
            while ((count = fos.read(buffer)) > 0)
            {
                os.write(buffer, 0, count);
            }
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                
            }
            catch (IOException e)
            {
                
                LOG.error(e.getMessage(), e);
                
            }
            
        }
    }
    
    /**
     * 项目文件上传
     * 
     * @param request
     * @param bean
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/projectPersonalUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject projectPersonalUpload(HttpServletRequest request, @RequestParam(value = "parent_id", required = false) String parent_id,
        @RequestParam(value = "type", required = false) String type, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (null == fileList || fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
            }
            resultArray = projectPersonalUpload(parent_id, fileList, token, type);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    /**
     * 文库自己上传
     * 
     * @param parent_id
     * @param fileList
     * @param token
     * @param library_type
     * @return
     * @Description:
     */
    private JSONArray projectPersonalUpload(String parent_id, List<MultipartFile> fileList, String token, String type)
    {
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                InfoVo info = TokenMgr.obtainInfo(token);
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                JSONObject employee = employeeAppService.queryEmployee(info.getEmployeeId(), info.getCompanyId());
                attachmentJson.put("upload_by", employee.get("employee_name") == null ? "" : employee.get("employee_name"));
                attachmentJson.put("upload_time", System.currentTimeMillis());
                String fileUrlPath = FileDaoUtil.addProjectPersonalFile(fileNameNew, file.getSize(), parent_id, token, type);
                if (StringUtils.isEmpty(fileUrlPath))
                {
                    return null;
                }
                
                String fileUrl = OSSUtil.getInstance().addFile(Constant.FLIE_LIBRARY_NAME, file.getInputStream(), fileUrlPath, file.getSize());
                if (!StringUtils.isEmpty(fileUrl))
                {// 有可能会上传失败！
                    String filePathUrl = config.getString("teamface.file.url");
                    filePathUrl = filePathUrl.concat("fileName=").concat(fileUrl);
                    attachmentJson.put("file_url", filePathUrl);
                }
                else
                {
                    ServiceException serviceException = new ServiceException(resultCode.get("common.upload.file.interrupt"), resultCode.getMsgZh("common.upload.file.interrupt"));
                    throw serviceException;
                }
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return resultArray;
    }
    
    /**
     * 
     * @param request
     * @param response
     * @param id
     * @param tokenPara
     * @param tokenHeader
     * @Description:项目下载文件下载
     */
    @RequestMapping(value = "/common/file/projectDownload", method = RequestMethod.GET)
    public void projectDownload(HttpServletRequest request, HttpServletResponse response, @RequestParam(required = true) Long id, @RequestParam(required = false) Integer type,
        @RequestParam(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenPara,
        @RequestHeader(value = DataTypes.REQUEST_HEADER_TOKEN, required = false) String tokenHeader)
    {
        if (StringUtil.isEmpty(tokenPara) && StringUtil.isEmpty(tokenHeader))
        {
            return;
        }
        String token = StringUtil.isEmpty(tokenPara) ? tokenHeader : tokenPara;
        InfoVo info = TokenMgr.obtainInfo(token);
        InputStream is = null;
        BufferedInputStream fos = null;
        OutputStream os = null;
        try
        {
            StringBuilder queryBuilder = new StringBuilder();
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            queryBuilder.append(" select * from ").append(libraryTable).append(" where id = ").append(id);
            JSONObject fileJson = DAOUtil.executeQuery4FirstJSON(queryBuilder.toString());
            if (null == fileJson)
            {
                return;
            }
            String fileUrl = fileJson.getString("url");
            String fileName = fileJson.getString("file_name");
            Long size = fileJson.getLongValue("size");
            response.setCharacterEncoding("UTF-8");
            response.setHeader("content-disposition", "attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
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
            os.flush();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        finally
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
                
            }
            catch (IOException e)
            {
                
                LOG.error(e.getMessage(), e);
                
            }
        }
    }
    
    /**
     * 移动端 banner 上传
     * 
     * @param request
     * @param token
     * @return
     * @Description:
     */
    @RequestMapping(value = "/common/file/companyBannerUpload", method = RequestMethod.POST)
    public @ResponseBody JSONObject companyBannerUpload(HttpServletRequest request, @RequestHeader(DataTypes.REQUEST_HEADER_TOKEN) String token)
    
    {
        JSONArray resultArray = new JSONArray();
        List<MultipartFile> fileList = new ArrayList<MultipartFile>();
        try
        {
            CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(request.getSession().getServletContext());
            // 检查form中是否有enctype="multipart/form-data"
            if (multipartResolver.isMultipart(request))
            {
                // 将request变成多部分request
                MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest)request;
                // 获取multiRequest 中所有的文件名
                Iterator<String> iter = multiRequest.getFileNames();
                while (iter.hasNext())
                {
                    // 一次遍历所有文件
                    MultipartFile multipartFile = multiRequest.getFile(iter.next().toString());
                    fileList.add(multipartFile);
                }
            }
            
            if (fileList.isEmpty())// 解析文件，文件可以放天RequestParameters里面，亦支持HttpServletRequest
                fileList = FileUtil.getFileList(request);
            // 如果上传文件不合法，直接一个文件都不传
            for (MultipartFile file : fileList)
            {
                String postfix = file.getOriginalFilename().substring(file.getOriginalFilename().lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))
                {// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.type.err");
                }
                if (file.getSize() > 102400)
                {
                    return JsonResUtil.getResultJsonByIdent("common.upload.file.size.error");
                }
            }
            resultArray = bannerUploadFile("company", fileList);
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
            return JsonResUtil.getResultJsonObject(resultCode.get("common.fail"), e.getMessage());
        }
        return JsonResUtil.getSuccessJsonObject(resultArray);
    }
    
    /**
     * 移动端banner
     * 
     * @param beanName
     * @param fileList
     * @return
     * @throws ServiceException
     * @Description:
     */
    private JSONArray bannerUploadFile(String beanName, List<MultipartFile> fileList)
        throws ServiceException
    {
        String buckName = Constant.COMPANY_LIBRARY_NAME;
        JSONArray resultArray = new JSONArray();
        try
        {
            int serialNumber = 0;
            for (MultipartFile file : fileList)
            {
                String fileNameNew = file.getOriginalFilename();
                String postfix = fileNameNew.substring(fileNameNew.lastIndexOf(".") + 1);
                if (FileBackListErum.getAllPostFix().contains(postfix))// 如果文件上传的格式在黑名单里面，那么直接继续下一条
                    continue;
                serialNumber++;
                JSONObject attachmentJson = new JSONObject();
                attachmentJson.put("original_file_name", fileNameNew);
                attachmentJson.put("file_name", fileNameNew);
                attachmentJson.put("file_type", postfix.toLowerCase());// 文件后缀，全部小写
                attachmentJson.put("file_size", file.getSize());// 文件大小，以B为单位
                attachmentJson.put("serial_number", serialNumber);// 文件的序列号，一次上传多张文件时，按顺序返回，序号从0开始
                String key = OSSUtil.getInstance().addFile(buckName, file.getInputStream(), fileNameNew, file.getSize());
                String fileUrl = imageFileUrl.concat("bean=").concat(beanName).concat("&fileName=").concat(key).concat("&fileSize=") + file.getSize();
                attachmentJson.put("file_url", fileUrl);
                resultArray.add(attachmentJson);
            }
        }
        catch (Exception e)
        {
            throw new ServiceException(e);
        }
        return resultArray;
    }
}
