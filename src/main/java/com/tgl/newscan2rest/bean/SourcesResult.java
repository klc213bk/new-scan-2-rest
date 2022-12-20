package com.tgl.newscan2rest.bean;

import java.util.List;

public class SourcesResult {

	String errorCode = "0000";
	List<String> errorMessages;
	List<String> sourceList;
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
	public List<String> getSourceList() {
		return sourceList;
	}
	public void setSourceList(List<String> sourceList) {
		this.sourceList = sourceList;
	}
	
	
}
