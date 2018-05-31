package com.teamface.common.util;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;
import java.util.Map;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

public class TwoMicroCodeUtil
{
    private static final Logger LOG = LoggerFactory.getLogger(TwoMicroCodeUtil.class);
    
    private static final int BLACK = 0xFF000000;
    
    private static final int WHITE = 0xFFFFFFFF;
    
    private static final String CHAR_CODE = "utf-8";
    
    private static final String FORMAT = "png";
    
    private TwoMicroCodeUtil()
    {
    }
    
    public static BufferedImage toBufferedImage(BitMatrix matrix)
    {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++)
        {
            for (int y = 0; y < height; y++)
            {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }
    
    public static void writeToFile(BitMatrix matrix, String format, File file)
        throws IOException
    {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, file))
        {
            throw new IOException("Could not write an image of format " + format + " to " + file);
        }
    }
    
    public static void writeToStream(BitMatrix matrix, String format, OutputStream stream)
        throws IOException
    {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, stream))
        {
            throw new IOException("Could not write an image of format " + format);
        }
    }
    
    /**
     * 
     * @param text 要转换成二维码的字符串如www.baidu.com
     * @param width
     * @param height
     * @param outDir 输出二维码图片目录(文件格式为png)
     * @return
     */
    public static String twoMicroCodeWrite(String text, int width, int height, String outDir)
    {
        if (!new File(outDir).isDirectory())
        {
            throw new IllegalStateException("不是有效的文件路径");
        }
        Map<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        // 内容所使用编码
        hints.put(EncodeHintType.CHARACTER_SET, CHAR_CODE);
        try
        {
            BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, hints);
            // 生成二维码
            String fileName = "png" + DateUtil.getLongDate() + ".png";
            File outFile = new File(outDir, fileName);
            writeToFile(bitMatrix, FORMAT, outFile);
            return fileName;
        }
        catch (Exception e)
        {
            LOG.error("生成二维码发生异常", e);
        }
        return "";
    }
    
    /**
     * 
     * @param absolutePath 二维码图片绝对路径
     * @return
     */
    public static String twoMicroCodeReader(String absolutePath)
    {
        File readFile = new File(absolutePath);
        if (!readFile.isFile())
        {
            throw new IllegalStateException("不是有效的文件");
        }
        BufferedImage image = null;
        Result result = null;
        try
        {
            image = ImageIO.read(readFile);
            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            
            Map<DecodeHintType, Object> hints = new Hashtable<DecodeHintType, Object>();
            // 注意要使用 utf-8，因为刚才生成二维码时，使用了utf-8
            hints.put(DecodeHintType.CHARACTER_SET, CHAR_CODE);
            result = new MultiFormatReader().decode(bitmap, hints);
            return result.getText();
        }
        catch (Exception e)
        {
            LOG.error(e.getMessage(), e);
        }
        return "";
    }
    
    public static void main(String[] args)
        throws Exception
    {
        // String text = "http://www.baidu.com";
        // twoMicroCodeWrite(text,300,300,"D:/tmp");
        String text = twoMicroCodeReader("D:\\tmp\\png20151228193623313.png");
        System.out.println(text);
    }
}
