package com.tgl.newscan2rest.bean;

public class ScanUploadStatus {
	int selectedIndex;
	
	private Integer code;
	
	private String description;
	
	ScanUploadStatus(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public int getSelectedIndex() {
		return selectedIndex;
	}

	public void setSelectedIndex(int selectedIndex) {
		this.selectedIndex = selectedIndex;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Integer getCode() {
		return code;
	}

	
}
