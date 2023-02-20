package com.tgl.newscan2rest.bean;

import com.tgl.newscan2rest.util.ImageRecordHelper;

class User {
	private int loginStatus;
	
	private ScanConfig scanConfig;
	
	private ImageRecordSet recordSet;
	
	private ImageRecordHelper recordSetHelper;
	
	public User() {
		loginStatus = LoginStatus.STATUS_NOT_USER_LOGGIN;
		recordSetHelper = ImageRecordHelper.getInstance();
	}

	public ScanConfig getScanConfig() {
		return scanConfig;
	}

	public void setScanConfig(ScanConfig scanConfig) {
		this.scanConfig = scanConfig;
	}

	public ImageRecordSet getRecordSet() {
		return recordSet;
	}

	public void setRecordSet(ImageRecordSet recordSet) {
		this.recordSet = recordSet;
	}

	public int getLoginStatus() {
		return loginStatus;
	}

	public ImageRecordHelper getRecordSetHelper() {
		return recordSetHelper;
	}
	
	
}
