package com.tgl.newscan2rest.bean;


public class User {
	private int loginStatus;
	
	private ScanConfig scanConfig;
	
	private ImageRecordSet recordSet;
	
	public User() {
		loginStatus = LoginStatus.STATUS_NOT_USER_LOGGIN;
	}

	public ScanConfig getScanConfig() {
		return scanConfig;
	}

	public void setScanConfig(ScanConfig scanConfig) {
		this.scanConfig = scanConfig;
	}

	public int getLoginStatus() {
		return loginStatus;
	}

	public void setLoginStatus(int loginStatus) {
		this.loginStatus = loginStatus;
	}

	public ImageRecordSet getRecordSet() {
		return recordSet;
	}

	public void setRecordSet(ImageRecordSet recordSet) {
		this.recordSet = recordSet;
	}

}
