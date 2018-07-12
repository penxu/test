package com.teamface.common.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;

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

import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.LayoutUtil;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;

/**
 * 
*@Title:
*@Description: 数据操作
*@Author:liupan  
*@Since:2018年6月8日
*@Version:1.1.0
 */
public class FileDaoUtil
{
    private static final Logger log = LogManager.getLogger(FileDaoUtil.class);
    
    /**
     * 文件库 公司个人上传
     * @param id
     * @param url
     * @param fileNameNew
     * @param size
     * @param token
     * @param type
     * @return
     * @Description:
     */
    public static String savaFileLibraryFile(String id, String url, String fileNameNew, long size, String token, String type, StringBuilder ids)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        StringBuilder key = new StringBuilder();
        try
        {
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
                return null;
            }
            log.debug("插入主表数据成功=====================================");
            Long currId = BusinessDAOUtil.geCurrval4Table("catalog", info.getCompanyId().toString());
            ids.append(currId);
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
                return null;
            }
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
        log.debug("插入历史版本成功=====================================");
        return key.toString();
    }
    
    /**
     * 文件 版本上传
     * 
     * @param id
     * @param url
     * @param fileNameNew
     * @param size
     * @param token
     * @param type
     * @return
     * @Description:
     */
    public static String uploadFileVersionFile(String id, String url, String fileNameNew, long size, String token, String type)
    {
        StringBuilder key = new StringBuilder();
        try
        {
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
                return null;
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
                return null;
            }
        }
        catch (NumberFormatException e)
        {
            log.error(e.getMessage(), e);
            return "";
        }
        return key.toString();
    }
    
    /**
     * 应用模块文件上传
     * 
     * @param beanName
     * @param name
     * @param size
     * @param applyId
     * @param token
     * @param modelId
     * @param data_id
     * @return
     * @Description:
     */
    public static String addApplyFile(String beanName, String name, long size, String applyId, String token,  String dataId)
    {
        StringBuilder key = new StringBuilder();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            
            String sql = "select * from application_module_" + info.getCompanyId() + " where english_name = '" + beanName + "'";
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(sql);
            if(jsonObject.isEmpty()) {
                return null;
            }
            String siffix = name.substring(name.lastIndexOf("."));
            key.append(applyId + Constant.APPLY_NAME).append("/").append(beanName + "/").append(System.currentTimeMillis()).append(siffix);
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into catalog_")
                .append(info.getCompanyId())
                .append("(model_id,parent_id,name,size,table_id,sign,url,siffix,type,create_by,create_time) values(")
                .append(dataId)
                .append(",")
                .append(jsonObject.getLong("id"))
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
            int count = DAOUtil.executeUpdate(insertBuilder.toString());
            if (count <= 0)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
        return key.toString();
    }
    
    /**
     * 项目文库自己上传
     * 
     * @param fileNameNew
     * @param size
     * @param parent_id
     * @param token
     * @param type
     * @return
     * @Description:
     */
    public static String addProjectPersonalFile(String fileNameNew, long size, String parent_id, String token, String type)
    {
        StringBuilder key = new StringBuilder();
        try
        {
            InfoVo info = TokenMgr.obtainInfo(token);
            String libraryTable = DAOUtil.getTableName("project_library", info.getCompanyId());
            StringBuilder queryBilder = new StringBuilder();
            // 获取上级目录
            queryBilder.append("select * from  ")
                .append(libraryTable)
                .append(" where data_id = ")
                .append(parent_id)
                .append(" and project_type = ")
                .append(Constant.CURRENCY_TWO)
                .append(" and library_type = ")
                .append(Constant.CURRENCY_ONE);
            JSONObject jsonObject = DAOUtil.executeQuery4FirstJSON(queryBilder.toString());
            String suffix = fileNameNew.substring(fileNameNew.lastIndexOf("."));
            key.append(jsonObject.getString("url")).append(System.currentTimeMillis()).append(suffix);
            Long id = BusinessDAOUtil.geCurrval4Table("project_library", info.getCompanyId().toString());
            StringBuilder insertBuilder = new StringBuilder();
            insertBuilder.append("insert into ")
                .append(libraryTable)
                .append("(project_type,data_id,file_name,size,url,parent_id,suffix,type,create_by,library_type,create_time) values(")
                .append(Constant.CURRENCY_THREE)
                .append(",")
                .append(id)
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
                .append(Constant.CURRENCY_ONE)
                .append(",")
                .append(System.currentTimeMillis())
                .append(")");
            int count = DAOUtil.executeUpdate(insertBuilder.toString());
            if (count <= 0)
            {
                return null;
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
            return null;
        }
        return key.toString();
    }
    
    /**
     * 打印上传
    * @param inputStream
    * @param postfix
    * @param contentType
    * @param downloadTem
    * @param bean
    * @param info
    * @return
    * @Description:
     */
    public static String printUpload(InputStream inputStream, String postfix,  String downloadTem, String bean, InfoVo info)
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
            String osName = System.getProperty("os.name").toLowerCase();
            if (osName.indexOf("linux") >= 0)
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
    private static void commAnalyze(String url, String bean, InfoVo info, String downloadTem, String postfix)
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
                                    cell.setCellValue(map.get(str) + "$" + map.get(content));
                                }
                            }
                            else
                            {
                                
                                // 存在相应的转换标识字段
                                if (null != map.get(value) && !"".equals(map.get(value)))
                                {
                                    cell.setCellValue(map.get(value));
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
            // 输出文件
            FileOutputStream fileOut = new FileOutputStream(url);
            wb.write(fileOut);
            fileOut.close();
            
        }
        catch (Exception e)
        {
            log.debug(" commAnalyze  异常 ", e);
        }
    }
    
    /**
     * 上传模版
    * @param inputStream
    * @param postfix
    * @param contentType
    * @param downloadTem
    * @return
    * @Description:
     */
    public static String importTemplateFile(InputStream inputStream, String postfix, String contentType, String downloadTem)
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
    
    /**
     * 更新文件库catalog表的记录，添加转换后的pdf路径，跟原来的id绑定
     * 
     * @param id
     * @param pdfurl
     * @Description:
     */
    public static void updatePDFUrlByTransfor(String id, String pdfurl, String token)
    {
        InfoVo info = TokenMgr.obtainInfo(token);
        Long companyId = info.getCompanyId();
        StringBuilder updateBuilder = new StringBuilder();
        String catalogTable = DAOUtil.getTableName("catalog", companyId);
        updateBuilder.append(" update ").append(catalogTable).append(" set pdf_url='").append(pdfurl).append("' where id=").append(id);
        DAOUtil.executeUpdate(updateBuilder.toString());
    }
}
