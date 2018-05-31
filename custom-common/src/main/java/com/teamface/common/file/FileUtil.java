package com.teamface.common.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import com.teamface.common.constant.ModuleEnum;


/**
 * 文件工具类
 * 
 * @author Liu
 * @date 2016-07-22
 * @version 1.0
 *
 */
public class FileUtil {
	/**
	 * 获取某个文件类的大小,单位为M
	 * 
	 * @author Liu
	 * @date 2016-07-22
	 * @param file
	 * @version 1.0
	 */
	public static double getDirSize(File file) {
		// TODO Auto-generated method stub

		if (file.exists()) {// 判断文件是否存在
			if (file.isDirectory()) {// 如果是目录则递归计算其内容的总大小
				File[] children = file.listFiles();
				double size = 0.00;
				for (File f : children)
					size += getDirSize(f);
				return size;
			} else {// 如果是文件则直接返回其大小,以“兆”为单位
				return (double) file.length() / 1024 / 1024;
			}
		} else {// 如果路径或者文件夹不存在
			System.err.println("文件或者文件夹不存在，请检查路径是否正确!");
			return 0.00;
		}
	}

	/**
	 * 递归删除某个目录下的所有子目录和文件
	 * 
	 * @author Liu
	 * @date 2016-074-27
	 * @param file
	 * @return boolean
	 * @version 1.0
	 */
	public static boolean deleteFile(File file) {
		// TODO Auto-generated method stub

		if (file.isDirectory()) {// 如果是个文件夹
			String[] children = file.list();
			for (int i = 0; i < children.length; i++) {
				boolean flag = deleteFile(new File(file, children[i]));
				if (!flag) {
					return false;
				}

			}
		}

		return file.delete();// 删除这个当前文件夹
	}

	/**
	 * 复制文件，把一个绝对路径下的文件复制到另一个绝对路径下、
	 * 
	 * @author Liu
	 * @date 2016-08-04
	 * @param originalAbsPath
	 * @param destinationAbsPath
	 * @return boolean
	 * @version 1.0
	 */
	public static boolean copyFile(String origAbsPath, String destAbsDir, String fileName) {
		// TODO Auto-generated method stub

		File origFile = new File(origAbsPath);

		if (!origFile.exists()) {// 如果文件不存在，直接返回false
			return false;
		}
		boolean flag = false;
		File destDir = new File(destAbsDir);
		if (!destDir.exists()) {// 如果目标文件夹不存在，那么直接新建
			destDir.mkdirs();
		}
		// 目标文件的绝对路径
		String destAbsPath = destAbsDir + "/" + fileName;
		File destFile = new File(destAbsPath);

		// 开始复制文件，用通道的方式，不用传统的字节流写入，读写速度快，缺点，占用内存线程
		FileInputStream fi = null;
		FileOutputStream fo = null;

		FileChannel in = null;
		FileChannel out = null;

		try {
			fi = new FileInputStream(origFile);
			fo = new FileOutputStream(destFile);
			in = fi.getChannel();
			out = fo.getChannel();
			in.transferTo(0, in.size(), out);
			flag = true;
		} catch (IOException ex) {
			flag = false;
			System.out.println(ex.getMessage());
		} finally {// 释方资源
			try {
				fi.close();
				in.close();
				fo.close();
				out.close();
			} catch (IOException ex) {
				System.out.println(ex.getMessage());
			}
		}
		return flag;
	}

	/**
	 * 上传文件到本地
	 * 
	 * @author Liu
	 * @date 2016-11-17
	 * @param file
	 * @param fileDir
	 * @param fileName
	 * @return String
	 * @version 1.0
	 */
	public static String upload(MultipartFile file, String fileDir, String fileName) {
		// TODO Auto-generated method stub

		// 拼接真实的目录
		String realDir = BaseProperties.getBaseProperties("absPath") + fileDir;
		String url = null;

		try {
			// 需要返回目录
			url = fileDir + fileName;

			// 上传目标文件夹，如果不存在就创建目标文件夹
			File dir = new File(realDir);
			if (!dir.exists()) {
				dir.mkdirs();
			}
			// 开始处理上传,把MultipartFile转为File,写文件到本地
			String realPath = realDir + fileName;
			File localFile = new File(realPath);
			file.transferTo(localFile);
		} catch (IOException e) {
			e.getMessage();
		}
		return url;

	}

	/**
	 * 获取一个路径下所属文件大小 ，可以是文件夹相对路径，也可以是文件的相对路径
	 * 
	 * @author Liu
	 * @param path
	 * @return double
	 * @version 1.0
	 */
	public static double getDirSizeByRelativePath(String relativePath) {
		// TODO Auto-generated method stub
		String absPath = BaseProperties.getBaseProperties("absPath") + relativePath;
		File file = new File(absPath);
		return FileUtil.getDirSize(file);
	}

	/**
	 * 删除路径下的某个文件 ，如果是路径下的文件夹，递归删除文件夹下的文件
	 * 
	 * @author Liu
	 * @date 2016-11-17
	 * @param relativePath
	 * @version 1.0
	 */
	public boolean deleteFileByRelativePaht(String relativePath) {
		// TODO Auto-generated method stub
		String absPath = BaseProperties.getBaseProperties("absPath") + relativePath;
		File file = new File(absPath);
		return FileUtil.deleteFile(file);
	}

	/**
	 * 复制一份文件到一个目录下
	 * 
	 * @author Liu
	 * @date 2016-11-17
	 * @param originalPath
	 * @param fileDir
	 * @param fileName
	 * @return String
	 * @version 1.0
	 */
	public String copyFile2Server(String origDir, String desDir, String fileName) {
		// TODO Auto-generated method stub
		if (origDir == null) {
			return null;
		}
		String origAbsPath = BaseProperties.getBaseProperties("absPath") + origDir;// 源文件的绝对路径
		String destAbsDir = BaseProperties.getBaseProperties("absPath") + desDir;// 目标文件的绝对路径
		boolean fg = FileUtil.copyFile(origAbsPath, destAbsDir, fileName);
		String url = null;
		if (fg) {
			url = desDir + fileName;
		}
		return url;
	}

	/**
	 * 获取文件列表
	 * 
	 * @param request
	 * @return
	 */
	public static List<MultipartFile> getFileList(HttpServletRequest request) {
		List<MultipartFile> fileList = new ArrayList<MultipartFile>();
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (multipartResolver.isMultipart(request)) {
			// 转换成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 取得request中的所有文件名
			Iterator<String> iter = multiRequest.getFileNames();
			while (iter.hasNext()) {
				// 取得上传文件
				MultipartFile file = multiRequest.getFile(iter.next());
				if (file != null) {
					// 取得当前上传文件的文件名称
					String myFileName = file.getOriginalFilename();
					// 如果名称不为“”,说明该文件存在，否则说明该文件不存在
					if (!"".equals(myFileName.trim())) {
						fileList.add(file);
					}
				}
			}
		}else{
			return null;
		}
		return fileList;
	}

	/**
	 * 获取文件map
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, MultipartFile> getFileMap(HttpServletRequest request) {
		Map<String, MultipartFile> map = new HashMap<String, MultipartFile>();

		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (multipartResolver.isMultipart(request)) {
			// 转换成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 取得request中的所有文件名
			map = multiRequest.getFileMap();
		}

		return map;
	}

	/**
	 * 获取文件map文件列表
	 * 
	 * @param request
	 * @return
	 */
	public static Map<String, List<MultipartFile>> getFileMapList(HttpServletRequest request) {

		Map<String, List<MultipartFile>> result = new HashMap<String, List<MultipartFile>>();

		// 从request中直接获取到map
		Map<String, MultipartFile> map = new HashMap<String, MultipartFile>();
		// 创建一个通用的多部分解析器
		CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
				request.getSession().getServletContext());
		// 判断 request 是否有文件上传,即多部分请求
		if (multipartResolver.isMultipart(request)) {
			// 转换成多部分request
			MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
			// 取得request中的所有文件名
			map = multiRequest.getFileMap();
		}

		// 把map转换为对应的选项KEY
		for (String key : map.keySet()) {

			String[] index = key.split("_");

			if (index != null && index.length > 1) {
				List<MultipartFile> itemList = result.get(index[0]);

				if (null == itemList) {
					itemList = new ArrayList<MultipartFile>();
					result.put(index[0], itemList);
				}

				itemList.add(Integer.parseInt(index[1]), map.get(key));
			}

		}

		return result;

	}

	public static void main(String[] args) {
		String modue = ModuleEnum.getDes(0);
		System.out.println("this modue is:" + modue);
		System.out.println("this ModuleEnum.company:" + ModuleEnum.company);
	}
}
