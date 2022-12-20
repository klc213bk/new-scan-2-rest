package com.tgl.newscan2rest.bean;

import java.util.List;

public class LoginResult {

	String errorCode = "0000";
	List<String> errorMessages;
	LoginStatus loginStatus;
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
	public LoginStatus getLoginStatus() {
		return loginStatus;
	}
	public void setLoginStatus(LoginStatus loginStatus) {
		this.loginStatus = loginStatus;
	}
	
	
}
