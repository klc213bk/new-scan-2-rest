package com.tgl.newscan2rest.bean;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeleteRecord {

	private int deleteIndex; // 0 base
	
	private String fileURL;

	
	public int getDeleteIndex() {
		return deleteIndex;
	}

	public void setDeleteIndex(int deleteIndex) {
		this.deleteIndex = deleteIndex;
	}

	public String getFileURL() {
		return fileURL;
	}

	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}
	
	

}
