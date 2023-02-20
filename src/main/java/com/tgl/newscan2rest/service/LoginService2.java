package com.tgl.newscan2rest.service;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
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
import com.tgl.newscan2rest.util.PropertiesCache;

//import javafx.concurrent.Service;
//import javafx.concurrent.Task;

@Service
public class LoginService2 {
	private static final Logger logger = LogManager.getLogger(LoginService2.class);
	
	private String processCode = null;
	private ScanConfig scanConfig = null;

	
	public LoginStatus logout() {
		EbaoClient.getInstance().close();
		processCode = LoginStatus.PROC_CODE_LOG_OUT;
		
		return new LoginStatus(
				processCode, 
				"",
				"",
				null
			);
	}
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

	        	storeProperties(_hostName, _userName);
	        	
	        	
	    		return new LoginStatus(
    				processCode, 
    				ObjectsUtil.isNotEmpty(connInfo.getServerName()) ? connInfo.getServerName() : _hostName,
    				connInfo.getRequestToken(),
    				scanConfig
    			);

	}
	
	private void storeProperties(String hostName, String userName) {
		PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.EBAO_HOST.propName(), hostName);
		PropertiesCache.getInstance().setProperty(PropertiesCache.PROP_KEY.EBAO_USER_NAME.propName(), userName);
		try {
			PropertiesCache.getInstance().flush();
		} catch (IOException e) {
			logger.error("無法寫入組態設定檔 " + PropertiesCache.PROPS_FILE_NAME + "！");
		}
	}
	public void testEbaoClient() throws EBaoException {
		EbaoClient eBaoClient = EbaoClient.getInstance();
		if (!eBaoClient.isClientInitialized()) {
			throw new EBaoException(EBaoException.Code.CONNECTION_NOT_INIT);
		}
	}
}
