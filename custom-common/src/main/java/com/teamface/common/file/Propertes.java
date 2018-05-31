package com.teamface.common.file;

import java.io.Serializable;


/**
 * <p>Title:Propertes</p>
 * <p>Description:(proprtes 配置文件模型)</p>
 * <p>Copyright: Copyright (c) 2016</p>
 * <p>Company: 汇聚华企</p>
 * @author  xuyunxian
 * @version v1.0 2016-8-16
 */

public class Propertes implements Serializable{

	private static final long serialVersionUID = -5265492093723490274L;
	private String key;
	private String values;
	
	public Propertes(String key, String values) {
		super();
		this.key = key;
		this.values = values;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValues() {
		return values;
	}
	public void setValues(String values) {
		this.values = values;
	}
	
	
}
