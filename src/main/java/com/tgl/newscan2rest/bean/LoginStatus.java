package com.tgl.newscan2rest.bean;

public class LoginStatus {

	public static final String PROC_CODE_NA = "N"; // 初始化
	public static final String PROC_CODE_SUCCESS = "S"; // 成功
	public static final String PROC_CODE_ERROR = "E"; // 連線方式/網址/逾時/其他錯誤
	public static final String PROC_CODE_WRONG_ID_PWD = "W"; // 使用者帳號/密碼錯誤
	public static final String PROC_CODE_PERMISSION_DENY = "P"; // 沒有權限
	public static final String PROC_CODE_SYSTEM_ERROR = "F"; // 系統錯誤
	public static final String PROC_CODE_LOG_OUT = "O"; // 登出

	public static final int STATUS_NOT_USER_LOGGIN = 0; // 未登入
	public static final int STATUS_OFF_LINE = 1; // 離線使用
	public static final int STATUS_LOGGED_IN = 2; // 已登入

	private String processCode;
	private String serverName;
	private String token;
	private ScanConfig config;

	public LoginStatus(String processCode, String serverName, String token, ScanConfig config) {
		super();
		this.processCode = processCode;
		this.serverName = serverName;
		this.token = token;
		this.config = config;
	}

	public String getProcessCode() {
		return processCode;
	}

	public void setProcessCode(String processCode) {
		this.processCode = processCode;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public ScanConfig getConfig() {
		return config;
	}

	public void setConfig(ScanConfig config) {
		this.config = config;
	}

}
