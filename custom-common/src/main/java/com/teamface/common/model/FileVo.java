package com.teamface.common.model;

import java.util.List;

public class FileVo {
	private Long id;
	private  String  color;
	private  String  name;
	private Long  table_id;
	private Long parent_id;
	private  String url;
	private List<FileVo> childList;
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getColor() {
		return color;
	}

	public void setColor(String color) {
		this.color = color;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public Long getTable_id() {
		return table_id;
	}

	public void setTable_id(Long table_id) {
		this.table_id = table_id;
	}

	public Long getParent_id() {
		return parent_id;
	}

	public void setParent_id(Long parent_id) {
		this.parent_id = parent_id;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public List<FileVo> getChildList() {
		return childList;
	}

	public void setChildList(List<FileVo> childList) {
		this.childList = childList;
	}

	

}
