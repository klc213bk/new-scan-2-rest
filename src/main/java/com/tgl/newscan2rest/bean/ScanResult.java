package com.tgl.newscan2rest.bean;

import java.util.List;

public class ScanResult {

	String errorCode;
	String errorMessage;
	
	public ScanResult(String errorCode, String errorMessage) {
		this.errorCode = errorCode;
		this.errorMessage = errorMessage;
	}
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}
	public void setErrorMessage(String errorMessage) {
		this.errorMessage = errorMessage;
	}
	
	public String toJsonString() {
		return "{\"errorCode\":" + errorCode + "\",\"errorMessage\":null}";
	}
}
