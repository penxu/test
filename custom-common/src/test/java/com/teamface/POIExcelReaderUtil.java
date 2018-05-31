package com.teamface;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * 读取包含下拉框的Excel的工具类
 */
public class POIExcelReaderUtil
{
    
    /**
     * 读取Excel数据内容
     * 
     * @param InputStream
     * @return Map 包含单元格数据内容的Map对象
     */
    public static Map<Integer, String> readExcelContent(InputStream is)
    {
        Map<Integer, String> content = new HashMap<Integer, String>();
        String str = "";
        HSSFWorkbook wb = null;
        try
        {
            wb = new HSSFWorkbook(is);
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        Sheet sheet = wb.getSheetAt(0);
        Cell cell = sheet.getRow(0).getCell(3);
        // 得到总行数
        int rowNum = sheet.getLastRowNum();
        // System.out.println("rowNum:" + rowNum);
        
        Row row = sheet.getRow(0);
        int colNum = row.getPhysicalNumberOfCells();
        // System.out.println("colNum:" + colNum);
        // 正文内容应该从第二行开始,第一行为表头的标题
        for (int i = 1; i <= rowNum; i++)
        {
            row = sheet.getRow(i);
            if (row == null)
            {
                content.put(i, str);
                continue;
            }
            int j = 0;
            while (j < colNum)
            {
                // 每个单元格的数据内容用"-"分割开，以后需要时用String类的replace()方法还原数据
                // 也可以将每个单元格的数据设置到一个javabean的属性中，此时需要新建一个javabean
                // str += getStringCellValue(row.getCell((short) j)).trim() +
                // "-";
                if (((row.getCell((short)j)) == null) || ("".equals(row.getCell((short)j))))
                {
                    str += "";
                    if (j == 0)
                    {
                        break;
                    }
                }
                else
                {
                    str += getCellFormatValue(row.getCell((short)j)).trim() + "-";
                }
                j++;
            }
            content.put(i, str);
            str = "";
        }
        return content;
    }
    
    /**
     * 根据HSSFCell类型设置数据
     * 
     * @param cell
     * @return
     */
    private static String getCellFormatValue(Cell cell)
    {
        String cellvalue = "";
        if (cell != null)
        {
            // 判断当前Cell的Type
            switch (cell.getCellType())
            {
                // 如果当前Cell的Type为NUMERIC
                case HSSFCell.CELL_TYPE_NUMERIC:
                case HSSFCell.CELL_TYPE_FORMULA:
                {
                    // 判断当前的cell是否为Date
                    if (HSSFDateUtil.isCellDateFormatted(cell))
                    {
                        // 如果是Date类型则，转化为Data格式
                        
                        // 方法1：这样子的data格式是带时分秒的：2011-10-12 0:00:00
                        // cellvalue = cell.getDateCellValue().toLocaleString();
                        
                        // 方法2：这样子的data格式是不带带时分秒的：2011-10-12
                        Date date = cell.getDateCellValue();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        cellvalue = sdf.format(date);
                        
                    }
                    // 如果是纯数字
                    else
                    {
                        // 取得当前Cell的数值
                        cellvalue = String.valueOf(cell.getNumericCellValue());
                    }
                    break;
                }
                    // 如果当前Cell的Type为STRIN
                case HSSFCell.CELL_TYPE_STRING:
                    // 取得当前的Cell字符串
                    cellvalue = cell.getRichStringCellValue().getString();
                    break;
                // 默认的Cell值
                default:
                    cellvalue = " ";
            }
        }
        else
        {
            cellvalue = "";
        }
        return cellvalue;
        
    }
    
    public static void main(String[] args)
        throws FileNotFoundException
    {
        // InputStream is2 = new FileInputStream("f:\\用户信息导入模板2016-12-21
        // 17_34_01.xls");
        // List<AccountImport> list = AccountImport.transferToEntity(is2);
        // for(AccountImport ai : list) {
        // System.out.println(ai.getAccount());
        // }
        
    }
    
}
