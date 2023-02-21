package com.tgl.newscan2rest.bean;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UploadResult {

	ScanResult validateBasicResult;
	
	ScanResult validateFilePageResult;
	
	ScanResult saveTiffDataResult;
	
	ScanResult checkLoginResult;
	
	List<ScanUploadStatus> scanUploadStatusList;
	
	ScanResult removeUploadedItemsResult;

	public ScanResult getValidateBasicResult() {
		return validateBasicResult;
	}

	public void setValidateBasicResult(ScanResult validateBasicResult) {
		this.validateBasicResult = validateBasicResult;
	}

	public ScanResult getValidateFilePageResult() {
		return validateFilePageResult;
	}

	public void setValidateFilePageResult(ScanResult validateFilePageResult) {
		this.validateFilePageResult = validateFilePageResult;
	}

	public ScanResult getSaveTiffDataResult() {
		return saveTiffDataResult;
	}

	public void setSaveTiffDataResult(ScanResult saveTiffDataResult) {
		this.saveTiffDataResult = saveTiffDataResult;
	}

	public ScanResult getCheckLoginResult() {
		return checkLoginResult;
	}

	public void setCheckLoginResult(ScanResult checkLoginResult) {
		this.checkLoginResult = checkLoginResult;
	}

	public List<ScanUploadStatus> getScanUploadStatusList() {
		return scanUploadStatusList;
	}

	public void setScanUploadStatusList(List<ScanUploadStatus> scanUploadStatusList) {
		this.scanUploadStatusList = scanUploadStatusList;
	}

	public ScanResult getRemoveUploadedItemsResult() {
		return removeUploadedItemsResult;
	}

	public void setRemoveUploadedItemsResult(ScanResult removeUploadedItemsResult) {
		this.removeUploadedItemsResult = removeUploadedItemsResult;
	}
	
	
}
