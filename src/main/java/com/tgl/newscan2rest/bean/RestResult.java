package com.tgl.newscan2rest.bean;

public class RestResult {
	String errorCode;
	String errorMessage;
	
	public RestResult(String errorCode, String errorMessage) {
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
