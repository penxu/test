package com.teamface.common.constant;

import java.util.ArrayList;
import java.util.List;

/**
 * 上传文件格式黑名单
 * 
 * @author Liu
 * @date 2016-12-26
 * @version v1
 */
public enum FileBackListErum {

	EXE(1, "exe"), SH(2, "sh"), BAT(3, "bat"),COM(4,"com"),SYS(5,"sys"),LNK(6,"lnk"),PIF(7,"pif"),SCR(8,"scr");

	private int dex;
	private String postFix;

	public int getDex() {
		return dex;
	}

	public void setDex(int dex) {
		this.dex = dex;
	}

	public String getPostFix() {
		return postFix;
	}

	public void setPostFix(String postFix) {
		this.postFix = postFix;
	}

	private FileBackListErum(int dex, String postFix) {
		this.dex = dex;
		this.postFix = postFix;
	}

	/**
	 * 根据某个下标取黑名单后缀
	 * 
	 * @author Liu
	 * @date 2016-12-26
	 * @param index
	 * @return string
	 */
	public static String getPostFix(int index) {
		for (FileBackListErum ae : FileBackListErum.values()) {
			if (index == ae.getDex()) {
				return ae.getPostFix();
			}
		}
		return null;
	}

	/**
	 * 获取所有的上传文件黑名称列表
	 * 
	 * @author Liu
	 * @date 2016-12-26
	 * @return list
	 */
	public static List<String> getAllPostFix() {
		List<String> list = new ArrayList<String>();
		for (FileBackListErum ae : FileBackListErum.values()) {
			list.add(ae.getPostFix());
		}
		return list;
	}

}
