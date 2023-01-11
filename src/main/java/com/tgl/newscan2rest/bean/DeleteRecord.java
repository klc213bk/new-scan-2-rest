package com.tgl.newscan2rest.bean;

public class DeleteRecord {

	private int deleteIndex; // 0 base
	
	private String scanOrder;
	
	private String fileURL;

	
	public int getDeleteIndex() {
		return deleteIndex;
	}

	public void setDeleteIndex(int deleteIndex) {
		this.deleteIndex = deleteIndex;
	}

	public String getScanOrder() {
		return scanOrder;
	}

	public void setScanOrder(String scanOrder) {
		this.scanOrder = scanOrder;
	}

	public String getFileURL() {
		return fileURL;
	}

	public void setFileURL(String fileURL) {
		this.fileURL = fileURL;
	}
	
	

}
