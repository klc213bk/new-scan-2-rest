package com.tgl.newscan2rest.bean;

import java.util.List;

public class ScanConfigResult {

	String errorCode = "0000";
	String errorMessages;
	ScanConfig scanConfig;
	
	public String getErrorCode() {
		return errorCode;
	}
	public void setErrorCode(String errorCode) {
		this.errorCode = errorCode;
	}
	public String getErrorMessages() {
		return errorMessages;
	}
	public void setErrorMessages(String errorMessages) {
		this.errorMessages = errorMessages;
	}
	public ScanConfig getScanConfig() {
		return scanConfig;
	}
	public void setScanConfig(ScanConfig scanConfig) {
		this.scanConfig = scanConfig;
	}
	
	
	
}
