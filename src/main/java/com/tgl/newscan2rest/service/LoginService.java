package com.tgl.newscan2rest.service;

import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.bean.ConnectionInfo;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.ScanConfig;
//import com.tgl.scan.main.http.EBaoException;
//import com.tgl.scan.main.http.EbaoClient;
//import com.tgl.scan.main.util.ObjectsUtil;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.http.EBaoException;
import com.tgl.newscan2rest.http.EbaoClient;
import com.tgl.newscan2rest.util.ObjectsUtil;

//import javafx.concurrent.Service;
//import javafx.concurrent.Task;

@Service
public class LoginService {

	private String processCode = null;
	private ScanConfig scanConfig = null;

	public LoginStatus login(LoginRequest loginRequest) throws EBaoException {
		final String _hostName = loginRequest.getHostName();
		if (_hostName == null) {
			throw new IllegalStateException("hostName property value is null");
		}
		final String _hostUrl = loginRequest.getHostUrl();
		if (_hostUrl == null) {
			throw new IllegalStateException("hostUrl property value is null");
		}
		final String _userName = loginRequest.getUserName();
		if (_userName == null) {
			throw new IllegalStateException("userName property value is null");
		}
		final String _password = loginRequest.getPassword();
		if (_password == null) {
			throw new IllegalStateException("password property value is null");
		}

		processCode = LoginStatus.PROC_CODE_NA;

		EbaoClient eBaoClient = EbaoClient.getInstance()
	                .setHostUrl(_hostUrl)
	                .setUserName(_userName)
	                .setPassword(_password)
	                .init();
				ConnectionInfo connInfo = eBaoClient.connect();
				boolean scanAllowed = eBaoClient.login();
				scanConfig = eBaoClient.getScanConfig();
	        	processCode = LoginStatus.PROC_CODE_SUCCESS;

	    		return new LoginStatus(
    				processCode, 
    				ObjectsUtil.isNotEmpty(connInfo.getServerName()) ? connInfo.getServerName() : _hostName,
    				connInfo.getRequestToken(),
    				scanConfig
    			);

	}

}
