package com.tgl.newscan2rest.bean;

import java.util.List;

public class ScanResult {

	String errorCode = "0000";
	List<String> errorMessages;
	String imageRecordSetFile;
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public List<String> getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(List<String> errorMessages) {
		this.errorMessages = errorMessages;
	}
	public String getImageRecordSetFile() {
		return imageRecordSetFile;
	}
	public void setImageRecordSetFile(String imageRecordSetFile) {
		this.imageRecordSetFile = imageRecordSetFile;
	}
	
	
	
}
