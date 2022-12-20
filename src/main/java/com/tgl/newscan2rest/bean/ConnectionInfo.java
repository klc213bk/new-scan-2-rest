package com.tgl.newscan2rest.bean;

public class ConnectionInfo {

	private String serverName;
	private String requestToken;

	public ConnectionInfo(String serverName, String requestToken) {
		super();
		this.serverName = serverName;
		this.requestToken = requestToken;
	}

	public String getServerName() {
		return serverName;
	}

	public void setServerName(String serverName) {
		this.serverName = serverName;
	}

	public String getRequestToken() {
		return requestToken;
	}

	public void setRequestToken(String requestToken) {
		this.requestToken = requestToken;
	}

}
