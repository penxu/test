package com.teamface.custom.excel;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration2.Configuration;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.DVConstraint;
import org.apache.poi.hssf.usermodel.HSSFDataValidation;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.ClientAnchor;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.DataValidation;
import org.apache.poi.ss.usermodel.DataValidationConstraint;
import org.apache.poi.ss.usermodel.DataValidationHelper;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Drawing;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.apache.poi.xssf.usermodel.XSSFDataValidationConstraint;
import org.apache.poi.xssf.usermodel.XSSFDataValidationHelper;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.web.context.ContextLoader;
import org.springframework.web.context.WebApplicationContext;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.teamface.common.constant.Constant;
import com.teamface.common.mq.RabbitMQServer;
import com.teamface.common.util.JobManager;
import com.teamface.common.util.PropertiesConfigObject;
import com.teamface.common.util.dao.BusinessDAOUtil;
import com.teamface.common.util.dao.DAOUtil;
import com.teamface.common.util.dao.JSONParser4SQL;
import com.teamface.common.util.dao.UtilDTO.Field;
import com.teamface.common.util.jwt.InfoVo;
import com.teamface.common.util.jwt.TokenMgr;
import com.teamface.custom.service.Thread.FirstThread;
import com.teamface.custom.service.employee.EmployeeAppService;

/**
 * 
 * @Title:
 * @Description:
 * @Author:liupan
 * @Since:2017年12月7日
 * @Version:1.1.0
 */
public class ExcelUtil
{
    // 最大行数
    private static final int maxRows = 10000;
    
    // 批次大小
    private static final int batchCount = 10000;
    
    static Logger log = Logger.getLogger(ExcelUtil.class);
    
    WebApplicationContext wac = ContextLoader.getCurrentWebApplicationContext();
    
    EmployeeAppService employeeAppService = wac.getBean(EmployeeAppService.class);
    
    RabbitMQServer rabbitMQServer = new RabbitMQServer();
    
    /**
     * 邮箱：=COUNTIF(O1,"*-*-*") 数字:=ISNUMBER(O1) 非空：=NOT(ISBLANK($A$5))
     * 
     * @param layoutJson
     * @param fullPath
     * @Description:
     */
    public int createTemplate(long companyId, JSONObject layoutJson, String fullPath)
    {
        Workbook workbook = null;
        int sum = 0;
        String bean = layoutJson.getString("bean");
        List<Field> fields = JSONParser4SQL.jsonParser4Table(layoutJson, false);
        try
        {
            int type = 0;
            if (fullPath.toLowerCase().endsWith("xlsx"))
            {
                workbook = new XSSFWorkbook();
                type = 1;
            }
            else if (fullPath.toLowerCase().endsWith("xls"))
            {
                workbook = new HSSFWorkbook();
                type = 2;
            }
            
            Sheet sheet = workbook.createSheet(DAOUtil.getTableName(bean, companyId));
            
            // 附属在WorkSheet上的一个对象， 其固定在一个单元格的左上角和右下角.
            // 得到一个POI的工具类
            CreationHelper factory = workbook.getCreationHelper();
            // 输入批注信息
            ClientAnchor anchor = factory.createClientAnchor();
            // 创建绘图对象
            Drawing p = sheet.createDrawingPatriarch();
            
            // 创建单元格格式
            CellStyle cs = workbook.createCellStyle();
            CellStyle cs2 = workbook.createCellStyle();
            // 创建两种字体
            Font f = workbook.createFont();
            Font f2 = workbook.createFont();
            
            // 创建第一种字体样式（用于列名）
            f.setFontHeightInPoints((short)10);
            f.setColor(IndexedColors.BLACK.getIndex());
            f.setBold(true);
            
            // 创建第二种字体样式（用于必填列）
            f2.setFontHeightInPoints((short)10);
            f2.setColor(IndexedColors.RED.getIndex());
            f2.setBold(true);
            
            cs.setFont(f);
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setBorderRight(BorderStyle.THIN);
            cs.setBorderTop(BorderStyle.THIN);
            cs.setBorderBottom(BorderStyle.THIN);
            cs.setAlignment(HorizontalAlignment.CENTER);
            
            cs2.setFont(f2);
            cs2.setBorderLeft(BorderStyle.THIN);
            cs2.setBorderRight(BorderStyle.THIN);
            cs2.setBorderTop(BorderStyle.THIN);
            cs2.setBorderBottom(BorderStyle.THIN);
            cs2.setAlignment(HorizontalAlignment.CENTER);
            
            int columnIndex = 0;
            Row row = sheet.createRow(0);
            if (columnIndex == 0)
            {
                // 隐藏列
                sheet.setColumnHidden(columnIndex, true);
                columnIndex++;
            }
            for (Field field : fields)
            {
                
                JSONObject json = field.layoutJson;
                JSONObject fieldJson = json.getJSONObject("field");
                if (fieldJson.getInteger("addView") == 0 && !json.getString("type").equals("identifier"))
                {
                    continue;
                }
                if (json.getString("type").equals("attachment") || json.getString("type").equals("subform") || json.getString("type").equals("picture"))
                {
                    continue;
                }
                if (field.name.indexOf("_v") != -1)
                {
                    continue;
                }
                // 创建第一行
                Cell cell = row.createCell(columnIndex);
                cell.setCellValue(field.label);
                if (json.getString("type").equals("identifier"))
                {
                    // 隐藏列
                    sheet.setColumnHidden(columnIndex, true);
                }
                
                Comment comment = p.createCellComment(anchor);
                RichTextString content = factory.createRichTextString(json.toJSONString());
                comment.setString(content);
                cell.setCellComment(comment);
                
                if (fieldJson.getInteger("fieldControl") == 1)
                {
                    cell.setCellStyle(cs2);
                    sheet.addValidationData(getValidateByFormula("NOT(ISBLANK(A1))", 1, maxRows, columnIndex, columnIndex, "非空错误提示", "当前列不能为空！", type, sheet));
                }
                else
                {
                    cell.setCellStyle(cs);
                    sheet.addValidationData(getValidationByMessage(1, maxRows, columnIndex, columnIndex, "备注", "当前列为可选项！", type, sheet));
                }
                
                JSONArray array = json.getJSONArray("entrys");
                if (array != null)
                {
                    int j = 0;
                    String[] values = new String[array.size()];
                    Iterator<Object> iterator = array.iterator();
                    while (iterator.hasNext())
                    {
                        JSONObject item = (JSONObject)iterator.next();
                        values[j++] = item.getString("label");
                    }
                    
                    sheet.addValidationData(getDataValidationByFormula(values, 1, maxRows, columnIndex, columnIndex, type, sheet));
                }
                if ("datetime".equals(json.getString("type")))
                {
                    sheet.addValidationData(getDataValidationByDate(fieldJson.getString("formatType"), 1, maxRows, columnIndex, columnIndex, type, sheet));
                }
                else if ("number".equals(json.getString("type")))
                {
                    sheet.addValidationData(getValidateByFormula("ISNUMBER(A1)", 1, maxRows, columnIndex, columnIndex, "数值类型错误提示", "当前列请输入数值类型！", type, sheet));
                }
                columnIndex++;
            }
            // 创建一输出文件流
            FileOutputStream fOut = new FileOutputStream(fullPath);
            // 把相应的Excel 工作簿存盘
            workbook.write(fOut);
            fOut.flush();
            // 操作结束，关闭文件
            fOut.close();
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        return 1;
    }
    
    public static HSSFWorkbook createWorkBook(Map<String, String> commentMap, List<Map<String, Object>> dataMapLS)
    {
        // 创建excel工作簿
        HSSFWorkbook wb = new HSSFWorkbook();
        // 创建第一个sheet
        Sheet sheet = wb.createSheet();
        
        // 创建两种单元格格式
        CellStyle cs = wb.createCellStyle();
        CellStyle cs2 = wb.createCellStyle();
        
        // 创建两种字体
        Font f = wb.createFont();
        Font f2 = wb.createFont();
        
        // 创建第一种字体样式（用于列名）
        f.setFontHeightInPoints((short)10);
        f.setColor(IndexedColors.BLACK.getIndex());
        f.setBold(true);
        
        // 创建第二种字体样式（用于值）
        f2.setFontHeightInPoints((short)10);
        f2.setColor(IndexedColors.BLACK.getIndex());
        
        // 设置第一种单元格的样式（用于列名）
        cs.setFont(f);
        cs.setBorderLeft(BorderStyle.THIN);
        cs.setBorderRight(BorderStyle.THIN);
        cs.setBorderTop(BorderStyle.THIN);
        cs.setBorderBottom(BorderStyle.THIN);
        cs.setAlignment(HorizontalAlignment.CENTER);
        
        // 设置第二种单元格的样式（用于值）
        cs2.setFont(f2);
        cs2.setBorderLeft(BorderStyle.THIN);
        cs2.setBorderRight(BorderStyle.THIN);
        cs2.setBorderTop(BorderStyle.THIN);
        cs2.setBorderBottom(BorderStyle.THIN);
        cs2.setAlignment(HorizontalAlignment.CENTER);
        if (dataMapLS.size() > 0)
        {
            List<String> comments = new ArrayList<>();
            List<String> columns = new ArrayList<>();
            Map<String, Object> firstRowData = dataMapLS.get(0);
            for (String column : firstRowData.keySet())
            {
                String comment = commentMap.get(column);
                comments.add(comment);
                columns.add(column);
            }
            // 创建第一行
            Row row = sheet.createRow((short)0);
            // 手动设置列宽。第一个参数表示要为第几列设；，第二个参数表示列的宽度，n为列高的像素数。
            for (int i = 0; i < comments.size(); i++)
            {
                sheet.setColumnWidth((short)i, (short)(35.7 * 150));
                // 设置列名
                Cell cell = row.createCell(i);
                cell.setCellValue(comments.get(i));
                cell.setCellStyle(cs);
            }
            // 设置每行每列的值
            for (int i = 0; i < dataMapLS.size(); i++)
            {
                Map<String, Object> rowData = dataMapLS.get(i);
                // Row 行,Cell 方格 , Row 和 Cell 都是从0开始计数的
                // 创建一行，在页sheet上
                Row row1 = sheet.createRow((short)i);
                // 在row行上创建一个方格
                for (short j = 0; j < columns.size(); j++)
                {
                    Object value = rowData.get(columns.get(j));
                    Cell cell = row1.createCell(j);
                    cell.setCellValue(value == null ? "" : value.toString());
                    cell.setCellStyle(cs2);
                }
            }
        }
        return wb;
    }
    
    public JSONObject getFileDate(String fullPath, String token)
        throws Exception
    {
        Workbook workbook = null;
        Workbook workbookError = null;
        int rowLoop = 0;// 错误行数
        PropertiesConfigObject properties = PropertiesConfigObject.getInstance();
        Configuration config = properties.getConfig();
        String downloadTem = config.getString("teamface.download.errortemplate"); // 获取存放路径
        String OS = System.getProperty("os.name").toLowerCase();
        String puthSuffix = "";
        if (OS.indexOf("linux") >= 0)
        {
            puthSuffix = downloadTem.concat("/") + System.currentTimeMillis() + ".txt";
        }
        else
        {
            puthSuffix = downloadTem.concat("//") + System.currentTimeMillis() + ".txt";
        }
        try
        {
            FileInputStream fis = new FileInputStream(fullPath);
            if (fullPath.toLowerCase().endsWith("xlsx"))
            {
                workbook = new XSSFWorkbook(fis);
                workbookError = new XSSFWorkbook();
            }
            else if (fullPath.toLowerCase().endsWith("xls"))
            {
                workbook = new HSSFWorkbook(fis);
                workbookError = new HSSFWorkbook();
            }
            InfoVo info = TokenMgr.obtainInfo(token);
            Map<Integer, List<String>> recheckMap = new HashMap<>();
            Map<Integer, JSONObject> cellComment = new HashMap<>();
            Sheet sheetError = workbookError.createSheet("错误日志");
            Sheet sheet = workbook.getSheetAt(0);
            String sheetName = sheet.getSheetName();
            
            StringBuilder insertSqlSB = new StringBuilder();
            StringBuilder tmpInsertSqlSB = new StringBuilder();
            insertSqlSB.append("insert into ").append(sheetName).append("(");
            // 拆分页码 获取bean
            String bean = sheetName.substring(0, sheetName.indexOf("_"));
            // 拆分页码 获取公司ID
            String compnyId = sheetName.substring(sheetName.indexOf("_") + 1, sheetName.length());
            // 得到一个POI的工具类
            CreationHelper factory = workbookError.getCreationHelper();
            // 创建绘图对象
            Drawing<?> p = sheetError.createDrawingPatriarch();
            // 附属在WorkSheet上的一个对象， 其固定在一个单元格的左上角和右下角.
            ClientAnchor anchor = factory.createClientAnchor();
            // 创建单元格格式
            CellStyle cs = workbookError.createCellStyle();
            CellStyle cs2 = workbookError.createCellStyle();
            
            cs.toString();
            // 创建两种字体
            Font f = workbookError.createFont();
            Font f2 = workbookError.createFont();
            
            // 创建第一种字体样式（用于列名）
            f.setFontHeightInPoints((short)10);
            f.setColor(IndexedColors.BLACK.getIndex());
            f.setBold(true);
            
            // 创建第二种字体样式（用于必填列）
            f2.setFontHeightInPoints((short)10);
            f2.setColor(IndexedColors.RED.getIndex());
            f2.setBold(true);
            
            cs.setFont(f);
            cs.setBorderLeft(BorderStyle.THIN);
            cs.setBorderRight(BorderStyle.THIN);
            cs.setBorderTop(BorderStyle.THIN);
            cs.setBorderBottom(BorderStyle.THIN);
            cs.setAlignment(HorizontalAlignment.CENTER);
            
            cs2.setFont(f2);
            cs2.setBorderLeft(BorderStyle.THIN);
            cs2.setBorderRight(BorderStyle.THIN);
            cs2.setBorderTop(BorderStyle.THIN);
            cs2.setBorderBottom(BorderStyle.THIN);
            cs2.setAlignment(HorizontalAlignment.CENTER);
            
            List<List<Object>> values = new ArrayList<>();
            int rowCount = 0; // 总共输入数据行数
            int rowCountError = 0; // 隐藏列在哪行
            // 得到行的迭代器
            int num = 0; // 计算下拉新增位数
            int sumNumber = 1; // 是否
            Row rowErr = sheetError.createRow(0);
            int cellNum = 0;
            int rowNum = sheet.getLastRowNum() + 1;
            StringBuilder writBuilder = new StringBuilder();
            // 循环每一行
            loop1: for (int k = 0; k < rowNum; k++)
            {
                int cellCount = 0;
                // 得到一行对象
                Row row = sheet.getRow(k);
                List<Object> valueLS = new ArrayList<>();
                if (k == 0)
                {
                    cellNum = row.getLastCellNum();
                }
                // 循环每一列
                for (int j = 1; j < cellNum; j++)
                {
                    // 得到单元格对象
                    Cell cell = row.getCell(j);
                    String value = null;
                    if (rowCount == 0)
                    {
                        value = cell.getStringCellValue();
                        Cell cellErr = rowErr.createCell(cellCount);
                        cellErr.setCellStyle(cs2);
                        Comment comment = cell.getCellComment();
                        RichTextString content = comment.getString();
                        cellErr.setCellValue(value);
                        cellErr.setCellComment(comment);
                        JSONObject object = JSONObject.parseObject(content.getString());
                        String type = object.getString("type");
                        if (type.equals("text") || type.equals("phone") || type.equals("email"))
                        {
                            Integer rechecking = object.getJSONObject("field").getInteger("repeatCheck");
                            if (rechecking.equals(2))
                            {
                                recheckMap.put(cellCount, new ArrayList<String>());
                            }
                        }
                        cellComment.put(cellCount, object);
                        if (type.equals("identifier"))
                        {
                            // 隐藏列
                            sheetError.setColumnHidden(cellCount, true);
                            rowCountError = cellCount;
                        }
                        if (tmpInsertSqlSB.length() > 0)
                        {
                            tmpInsertSqlSB.append(",");
                        }
                        tmpInsertSqlSB.append(object.getString("name"));
                        if (Constant.TYPE_PICKLIST.equals(type) || Constant.TYPE_MULTI.equals(type) || Constant.TYPE_MUTLI_PICKLIST.equals(type))
                        {
                            num++;
                            tmpInsertSqlSB.append(",").append(object.getString("name")).append(Constant.PICKUP_VALUE_FIELD_SUFFIX);
                        }
                        if ((cellCount + sumNumber) == row.getPhysicalNumberOfCells())
                        {
                            // 增加默认5个字段插入
                            tmpInsertSqlSB.append(",").append(Constant.FIELD_DEL_STATUS);
                            tmpInsertSqlSB.append(",").append(Constant.FIELD_CREATE_BY);
                            tmpInsertSqlSB.append(",").append(Constant.FIELD_CREATE_TIME);
                            tmpInsertSqlSB.append(",").append(Constant.FIELD_MODIFY_BY);
                            tmpInsertSqlSB.append(",").append(Constant.FIELD_MODIFY_TIME);
                            insertSqlSB.append(tmpInsertSqlSB).append(") values(");
                            StringBuilder pvalueSB = new StringBuilder();
                            for (int i = 0; i <= cellCount + num + 5; i++)
                            {
                                if (pvalueSB.length() > 0)
                                {
                                    pvalueSB.append(",");
                                }
                                pvalueSB.append("?");
                            }
                            insertSqlSB.append(pvalueSB).append(")");
                        }
                    }
                    else
                    {
                        JSONObject object = cellComment.get(cellCount);
                        String type = object.getString("type");
                        if (null == cell)
                        {
                            value = null;
                        }
                        else
                        {
                            if ("number".equals(type))
                            {
                                double priceMoney = cell.getNumericCellValue();
                                value = String.valueOf(priceMoney);
                            }
                            else if ("datetime".equals(type))
                            {
                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                                Date time = cell.getDateCellValue();
                                value = sdf.format(time);
                            }
                            else if ("phone".equals(type))
                            {
                                DecimalFormat df = new DecimalFormat("#");
                                value = df.format(cell.getNumericCellValue());
                            }
                            else
                            {
                                
                                switch (cell.getCellType())
                                {
                                    case Cell.CELL_TYPE_STRING:
                                        value = cell.getStringCellValue();
                                        break;
                                    case Cell.CELL_TYPE_NUMERIC:
                                        if (DateUtil.isCellDateFormatted(cell))
                                        {
                                            Date date = cell.getDateCellValue();
                                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                                            value = df.format(date);
                                        }
                                        else
                                        {
                                            value = String.valueOf(cell.getNumericCellValue());
                                        }
                                        break;
                                    case Cell.CELL_TYPE_FORMULA:
                                        cell.setCellType(Cell.CELL_TYPE_STRING);
                                        value = cell.getStringCellValue();
                                        break;
                                    
                                    default:
                                        break;
                                }
                                
                            }
                        }
                        boolean checking = dealChecking(valueLS,
                            object,
                            row,
                            sheetError,
                            recheckMap.get(cellCount),
                            value,
                            k+1,
                            cellCount,
                            cs,
                            factory,
                            anchor,
                            bean,
                            compnyId,
                            rowCountError,
                            writBuilder);
                        if (!checking)
                        {
                            
                            File writename = new File(puthSuffix); // 相对路径，如果没有则要建立一个新的output。txt文件
                            writename.createNewFile(); // 创建新文件
                            BufferedWriter out = new BufferedWriter(new FileWriter(writename));
                            out.write(writBuilder.toString()); // \r\n即为换行
                            out.flush(); // 把缓存区内容压入文件
                            out.close(); // 最后记得关闭文件
                            rowLoop++;
                            rowCount++;
                            continue loop1;
                        }
                    }
                    cellCount++;
                }
                if (valueLS.size() > 0)
                {
                    valueLS.add(0);
                    valueLS.add(info.getEmployeeId());
                    valueLS.add(System.currentTimeMillis());
                    valueLS.add(info.getEmployeeId());
                    valueLS.add(System.currentTimeMillis());
                    values.add(valueLS);
                }
                rowCount++;
            }
            if (values.size() > 0)
            {
                if (rowLoop <= 0)
                {
                    // 添加数据
                    int sum = DAOUtil.executeUpdate(insertSqlSB.toString(), values, batchCount);
                    if (sum > 0)
                    {
                        // 运行规则
                        JSONObject josn = new JSONObject();
                        String sql = "select id from " + sheetName + " order by  id desc limit " + sum;
                        List<JSONObject> jsonObject = DAOUtil.executeQuery4JSON(sql);
                        josn.put("token", token);
                        josn.put("bean", sheetName);
                        josn.put("trigger", 0);
                        josn.put("id", jsonObject);
                        rabbitMQServer.sendMessage("allot", josn.toJSONString());
                        JobManager.getInstance().submitJob(new FirstThread());
                    }
                }
            }
        }
        catch (Exception e)
        {
            log.error(e.getMessage(), e);
        }
        JSONObject object = new JSONObject();
        object.put("path", puthSuffix);
        object.put("size", rowLoop);
        return object;
        
    }
    
    private boolean dealChecking(List<Object> valueLS, JSONObject object, Row row, Sheet sheetError, List<String> cellReCheckLS, String value, int rowIndex, int columnIndex,
        CellStyle cs, CreationHelper factory, ClientAnchor anchor, String bean, String compnyId, int rowCountError, StringBuilder writBuilder)
    {
        JSONObject fieldJson = object.getJSONObject("field");
        String type = object.getString("type");
        String name = object.getString("name");
        if ("identifier".equals(type))
        { // 是否是隐藏列组件属性
            JSONObject numbering = object.getJSONObject("numbering");
            String fixedValue = numbering.getString("fixedValue");
            String dateFormat = numbering.getString("dateValue");
            String serialValue = numbering.getString("serialValue");
            String numberValue = BusinessDAOUtil.getNextAutoSequenceNumber(bean, name, fixedValue, dateFormat, serialValue, Long.valueOf(compnyId));
            if (StringUtils.isEmpty(numberValue))
            {
                valueLS.add(null);
            }
            else
            {
                valueLS.add(numberValue);
            }
            return true;
        }
        Integer required = fieldJson.getInteger("fieldControl");
        // 必填项
        if (required.equals(2) && (value == null || value.trim().length() == 0))
        {
            writBuilder.append("第".concat(rowIndex + "行").concat("当前行有必填项，值不能为空！\r\n"));
            return false;
        }
        if ("".equals(value) || null == value)
        {
            valueLS.add(value);
            return true;
        }
        if (value != null && value.trim().length() > 0)
        {
            if (type.equals("identifier") || type.equals("text") || type.equals("phone") || type.equals("email"))
            {
                // 检查重复
                Integer rechecking = fieldJson.getInteger("repeatCheck");
                if (rechecking.equals(2))
                {
                    if (cellReCheckLS.contains(value))
                    {
                        writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值不能重复！\r\n"));
                        return false;
                    }
                    String beanName = DAOUtil.getTableName(bean, compnyId);
                    String sql = "select count(*) from " + beanName + " where " + object.getString("name") + " = " + value;
                    int num = DAOUtil.executeCount(sql);
                    if (num > 0)
                    {
                        writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值不能重复！\r\n"));
                        return false;
                    }
                }
            }
            // 下拉 及复选框出来
            if (Constant.TYPE_PICKLIST.equals(type) || Constant.TYPE_MULTI.equals(type))
            {
                Map<String, JSONObject> entryMap = new HashMap<>();
                JSONArray entryArray = object.getJSONArray("entrys");
                if (entryArray != null)
                {
                    Iterator<Object> iterator = entryArray.iterator();
                    while (iterator.hasNext())
                    {
                        JSONObject item = (JSONObject)iterator.next();
                        String label = item.getString("label");
                        entryMap.put(label, item);
                    }
                }
                
                String[] arr = value.split(",");
                StringBuilder itemSB = new StringBuilder();
                StringBuilder itemValueSB = new StringBuilder();
                for (String str : arr)
                {
                    if (itemSB.length() > 0)
                    {
                        itemSB.append(",");
                        itemValueSB.append(",");
                    }
                    if (null != entryMap.get(str))
                    {
                        itemSB.append(entryMap.get(str));
                        itemValueSB.append(entryMap.get(str).get("value"));
                    }
                }
                if (itemSB.length() == 0)
                {
                    writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值必须来源于下拉列表，不能自行输入！\r\n"));
                    return false;
                }
                valueLS.add("[" + itemSB.toString() + "]");
                valueLS.add(itemValueSB.toString());
            }
            // 多级下拉处理
            else if (Constant.TYPE_MUTLI_PICKLIST.equals(type))
            {
                JSONArray entryArray = object.getJSONArray("entrys");
                List<Map<String, JSONObject>> entryMapList = new ArrayList<>();
                StringBuilder itemSB = new StringBuilder();
                StringBuilder itemValueSB = new StringBuilder();
                String[] arr = value.split("-");
                String labalKey = null;
                for (String str : arr)
                {
                    labalKey = str;
                    break;
                }
                if (entryArray.size() > 0)
                {
                    Iterator<Object> iterator = entryArray.iterator();
                    while (iterator.hasNext())
                    {
                        Map<String, JSONObject> entryMap = new HashMap<>();
                        JSONObject item = (JSONObject)iterator.next();
                        String label = item.getString("label");
                        if (label.equals(labalKey))
                        {
                            JSONObject labalValue = new JSONObject();
                            labalValue.put("label", item.getString("label"));
                            labalValue.put("color", item.getString("color"));
                            labalValue.put("value", item.getString("value"));
                            entryMap.put(label, labalValue);
                            entryMapList.add(entryMap);
                            JSONArray subListArray = item.getJSONArray("subList");
                            if (subListArray.size() > 0)
                            {
                                Iterator<Object> iterator1 = subListArray.iterator();
                                while (iterator1.hasNext())
                                {
                                    Map<String, JSONObject> entryMap1 = new HashMap<>();
                                    JSONObject item1 = (JSONObject)iterator1.next();
                                    String label1 = item1.getString("label");
                                    JSONObject labalValue1 = new JSONObject();
                                    labalValue1.put("label", item1.getString("label"));
                                    labalValue1.put("color", item1.getString("color"));
                                    labalValue1.put("value", item1.getString("value"));
                                    entryMap1.put(label1, labalValue1);
                                    entryMapList.add(entryMap1);
                                    JSONArray subListArray1 = item1.getJSONArray("subList");
                                    if (subListArray1.size() > 0)
                                    {
                                        Iterator<Object> iterator2 = subListArray1.iterator();
                                        while (iterator2.hasNext())
                                        {
                                            Map<String, JSONObject> entryMap2 = new HashMap<>();
                                            JSONObject item2 = (JSONObject)iterator2.next();
                                            String label2 = item2.getString("label");
                                            JSONObject labalValue2 = new JSONObject();
                                            labalValue2.put("label", item2.getString("label"));
                                            labalValue2.put("color", item2.getString("color"));
                                            labalValue2.put("value", item2.getString("value"));
                                            entryMap2.put(label2, labalValue2);
                                            entryMapList.add(entryMap2);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                for (String str : arr)
                {
                    for (Map<String, JSONObject> mapJson : entryMapList)
                    {
                        if (mapJson.get(str) != null)
                        {
                            if (itemSB.length() > 0)
                            {
                                itemSB.append(",");
                                itemValueSB.append("-");
                            }
                            itemSB.append(mapJson.get(str));
                            itemValueSB.append(mapJson.get(str).get("value"));
                            break;
                        }
                    }
                }
                
                String[] arrList = value.split(",");
                if (itemSB.length() == 0 || arrList.length > arr.length)
                {
                    writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值必须来源于下拉列表，不能自行输入！\r\n"));
                    return false;
                }
                valueLS.add("[" + itemSB.toString() + "]");
                valueLS.add(itemValueSB.toString());
            }
            // 关联关系处理
            else if ("reference".equals(type))
            {
                try
                {
                    JSONObject relevanceTrade = object.getJSONObject("relevanceModule");
                    String moduleName = relevanceTrade.getString("moduleName");
                    
                    JSONObject relevanceField = object.getJSONObject("relevanceField");
                    String fieldName = relevanceField.getString("fieldName");
                    
                    JSONObject josnMap = new JSONObject();
                    List<Object> relevanceWhere = object.getJSONArray("relevanceWhere");
                    JSONObject seniorWhere = object.getJSONObject("seniorWhere");
                    josnMap.put("relevanceWhere", relevanceWhere);
                    josnMap.put("seniorWhere", seniorWhere);
                    String commSql = JSONParser4SQL.getSeniorWhere4Relation(josnMap) == null ? "" : " and  " + JSONParser4SQL.getSeniorWhere4Relation(josnMap);
                    String moduleTable = DAOUtil.getTableName(moduleName, compnyId);
                    String sql = "select  id  from  " + moduleTable + " where " + fieldName + " = '" + value + "'" + commSql;
                    List<JSONObject> list = DAOUtil.executeQuery4JSON(sql);
                    if (list.size() > 1)
                    {
                        writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值错误！\r\n"));
                        return false;
                    }
                    Object objectCValue = DAOUtil.executeQuery4Object(sql);
                    valueLS.add(objectCValue);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值错误！\r\n"));
                    return false;
                }
            }
            // 人员
            else if ("personnel".equals(type))
            {
                try
                {
                    JSONObject employeeName = employeeAppService.getEmployeeByName(value, compnyId);
                    valueLS.add(employeeName.getLong("id"));
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值错误！\r\n"));
                    return false;
                }
            }
            else if ("datetime".equals(type))
            {
                String datetimeType = fieldJson.getString("formatType");
                try
                {
                    SimpleDateFormat sdf = new SimpleDateFormat(datetimeType);
                    long date = sdf.parse(value).getTime();
                    valueLS.add(date);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值必须是日期，格式不符合'" + datetimeType + "'格式规范! \r\n"));
                    return false;
                }
            }
            else if ("number".equals(type))
            {
                try
                {
                    float num = Float.parseFloat(value);
                    valueLS.add(num);
                }
                catch (Exception e)
                {
                    log.error(e.getMessage(), e);
                    writBuilder.append("第".concat(rowIndex + "行").concat("当前行有值必须是数字，值类型不符合要求! \r\n"));
                    return false;
                }
            }
            else
            {
                valueLS.add(value);
            }
        }
        return true;
    }
    
    private static void copyRow4Error(Row row, Sheet sheetError, int rowNum, int cellNum, CellStyle cs, Comment comment, int rowCountError)
    {
        Row rowErr = sheetError.createRow(rowNum + 1);
        // 得到列对象
        Iterator<Cell> cellIterator = row.cellIterator();
        int cellCount = 0;
        // 循环每一列
        int cellNumber = row.getLastCellNum();
        for (int j = 1; j < cellNumber; j++)
        {
            // 得到单元格对象
            Cell cell = row.getCell(j);
            String value = null;
            if (null == cell)
            {
                value = null;
            }
            else
            {
                switch (cell.getCellType())
                {
                    case Cell.CELL_TYPE_STRING:
                        value = cell.getStringCellValue();
                        break;
                    case Cell.CELL_TYPE_NUMERIC:
                        if (DateUtil.isCellDateFormatted(cell))
                        {
                            Date date = cell.getDateCellValue();
                            DateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                            value = df.format(date);
                        }
                        else
                        {
                            value = String.valueOf(cell.getNumericCellValue());
                        }
                        break;
                    case Cell.CELL_TYPE_FORMULA:
                        cell.setCellType(Cell.CELL_TYPE_STRING);
                        value = cell.getStringCellValue();
                        break;
                    
                    default:
                        break;
                }
                if (rowCountError > 0)
                {
                    if (cellCount == rowCountError)
                    { // 判断是否当前是隐藏列 是则推后一列写入
                        cellCount++;
                    }
                }
            }
            Cell cellError = rowErr.createCell(cellCount);
            cellError.setCellStyle(cs);
            if (cellNum == cellCount)
            {
                cellError.setCellComment(comment);
            }
            cellError.setCellValue(value);
            cellCount++;
        }
    }
    
    /**
     * 下拉型有效验证
     * 
     * @param type
     * @param sheet
     * 
     * @param formulaString
     * @param naturalRowIndex
     * @param naturalColumnIndex
     * @return
     */
    public static DataValidation getDataValidationByFormula(String[] explicitListValues, int firstRow, int lastRow, int firstCol, int lastCol, int type, Sheet sheet)
    {
        // 四个参数分别是：起始行、终止行、起始列、终止列
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidation data_validation_list;
        if (type == 1)
        {
            XSSFDataValidationConstraint constraint = new XSSFDataValidationConstraint(explicitListValues);
            DataValidationHelper help = new XSSFDataValidationHelper((XSSFSheet)sheet);
            // 数据有效性对象
            data_validation_list = help.createValidation(constraint, regions);
        }
        else
        {
            // 加载下拉列表内容
            DVConstraint constraint = DVConstraint.createExplicitListConstraint(explicitListValues);
            // 设置数据有效性加载在哪个单元格上。
            // 数据有效性对象
            data_validation_list = new HSSFDataValidation(regions, constraint);
        }
        // 设置输入信息提示信息
        data_validation_list.createPromptBox("下拉选择提示", "请使用下拉方式选择合适的值！");
        // 设置输入错误提示信息
        data_validation_list.createErrorBox("选择错误提示", "你输入的值未在备选列表中，请下拉选择合适的值！");
        return data_validation_list;
    }
    
    /**
     * 日期有效期验证
     * 
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @param sheet
     * @param type
     * @return
     * @Description:
     */
    public static DataValidation getDataValidationByDate(String format, int firstRow, int lastRow, int firstCol, int lastCol, int type, Sheet sheet)
    {
        DataValidation data_validation_list = null;
        // 设置数据有效性加载在哪个单元格上。
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        if (type == 1)
        {
            DataValidationConstraint constraint = new XSSFDataValidationConstraint(DataValidationConstraint.ValidationType.ANY, format);
            DataValidationHelper help = new XSSFDataValidationHelper((XSSFSheet)sheet);
            data_validation_list = help.createValidation(constraint, regions);
            
        }
        else
        {
            // 加载下拉列表内容
            DVConstraint constraint = DVConstraint.createDateConstraint(DVConstraint.OperatorType.BETWEEN, "1900-01-01", "5000-01-01", format);
            // 数据有效性对象
            data_validation_list = new HSSFDataValidation(regions, constraint);
        }
        // 设置输入信息提示信息
        data_validation_list.createPromptBox("日期格式提示", "请按照'" + format + "'格式输入日期值!");
        // 设置输入错误提示信息
        data_validation_list.createErrorBox("日期格式错误提示", "你输入的日期格式不符合'" + format + "'格式规范，请重新输入!");
        return data_validation_list;
    }
    
    /**
     * 数值型有效验证
     * 
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @return
     * @Description:
     */
    public static DataValidation getNumberValidationByDate(int firstRow, int lastRow, int firstCol, int lastCol)
    {
        
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint.createNumericConstraint(DVConstraint.ValidationType.DECIMAL, DVConstraint.OperatorType.GREATER_OR_EQUAL, "0", "0");
        // 设置数据有效性加载在哪个单元格上。
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        // 数据有效性对象
        DataValidation data_validation_list = new HSSFDataValidation(regions, constraint);
        // 设置输入错误提示信息
        data_validation_list.createErrorBox("输入值类型或大小错误提示", "数值型，请输入大于0 的数值!");
        return data_validation_list;
    }
    
    /**
     * 必填验证
     * 
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @return
     * @Description:
     */
    public static DataValidation getRequireValidationByDate(int firstRow, int lastRow, int firstCol, int lastCol)
    {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint.createNumericConstraint(DVConstraint.ValidationType.TEXT_LENGTH, DVConstraint.OperatorType.GREATER_OR_EQUAL, "0", "0");
        // 设置数据有效性加载在哪个单元格上。
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        // 数据有效性对象
        DataValidation data_validation_list = new HSSFDataValidation(regions, constraint);
        // 设置输入错误提示信息
        data_validation_list.createErrorBox("必填", "不能为空!");
        return data_validation_list;
    }
    
    /**
     * 长度有效验证
     * 
     * @param maxLength
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @return
     * @Description:
     */
    public static DataValidation getLengthValidationByDate(String maxLength, int firstRow, int lastRow, int firstCol, int lastCol)
    {
        // 加载下拉列表内容
        DVConstraint constraint = DVConstraint.createNumericConstraint(DVConstraint.ValidationType.TEXT_LENGTH, DVConstraint.OperatorType.BETWEEN, "0", maxLength);
        // 设置数据有效性加载在哪个单元格上。
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        // 数据有效性对象
        DataValidation data_validation_list = new HSSFDataValidation(regions, constraint);
        // 设置输入错误提示信息
        data_validation_list.createErrorBox("输入值长度错误提示", "长度介于[0," + maxLength + "]!");
        return data_validation_list;
    }
    
    /**
     * 给单元格添加提示内容
     * 
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @param title
     * @param text
     * @param type
     * @param sheet
     */
    public static DataValidation getValidationByMessage(int firstRow, int lastRow, int firstCol, int lastCol, String title, String text, int type, Sheet sheet)
    {
        // 设定在哪个单元格生效
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidation ret = null;
        if (type == 1)
        {
            DataValidationConstraint constraint = new XSSFDataValidationConstraint(DVConstraint.ValidationType.ANY, DVConstraint.OperatorType.IGNORED, null, null);
            DataValidationHelper help = new XSSFDataValidationHelper((XSSFSheet)sheet);
            // 数据有效性对象
            ret = help.createValidation(constraint, regions);
        }
        else
        {
            /*
             * 这里创建一个空的数据有效性校验 。这里不会将原有的数据校验覆盖
             */
            DVConstraint constraint = DVConstraint.createNumericConstraint(DVConstraint.ValidationType.ANY, DVConstraint.OperatorType.IGNORED, null, null);
            // 创建规则对象
            ret = new HSSFDataValidation(regions, constraint);
        }
        /*
         * 设置提示内容,标题,内容
         */
        ret.createPromptBox(title, text);
        return ret;
    }
    
    /**
     * 根据公式设置数据有效性
     * 
     * @param firstRow
     * @param lastRow
     * @param firstCol
     * @param lastCol
     * @param type
     * @param sheet
     * @return
     */
    public static DataValidation getValidateByFormula(String formul, int firstRow, int lastRow, int firstCol, int lastCol, String title, String errorMessage, int type, Sheet sheet)
    {
        CellRangeAddressList regions = new CellRangeAddressList(firstRow, lastRow, firstCol, lastCol);
        DataValidation validation = null;
        if (type == 1)
        {
            DataValidationConstraint constraint = new XSSFDataValidationConstraint(DataValidationConstraint.ValidationType.FORMULA, formul);
            // 创建公式约束（数据有效性）
            DataValidationHelper help = new XSSFDataValidationHelper((XSSFSheet)sheet);
            // 数据有效性对象
            validation = help.createValidation(constraint, regions);
        }
        else
        {
            // 创建公式约束（数据有效性）
            DVConstraint constraint = DVConstraint.createCustomFormulaConstraint(formul);
            // 数据有效性对象
            validation = new HSSFDataValidation(regions, constraint);
            
        }
        validation.createErrorBox(title, errorMessage);
        return validation;
    }
    
    public static void main(String[] args)
    {
        String path = "C:\\Users\\Administrator\\Desktop\\测试.xlsx";
        path = path.substring(path.lastIndexOf("\\") + 1, path.length());
        System.out.println(path);
    }
}