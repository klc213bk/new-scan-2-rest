package com.tgl.newscan2rest.controller;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.SessionAttribute;
import org.springframework.web.bind.support.SessionStatus;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.RestResult;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.UploadBean;
import com.tgl.newscan2rest.bean.User;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.http.EBaoException;
import com.tgl.newscan2rest.service.LoginService2;
import com.tgl.newscan2rest.service.UploadService;
import com.tgl.newscan2rest.util.ScanConfigUtil;

@RestController
public class UploadController {
	private static final Logger logger = LoggerFactory.getLogger(UploadController.class);
	
	private static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
	
	private static final String loginStatusJsonFile = CONFIG_DIR + File.separator + "loginStatus.json";
	
	@Autowired
	UploadService uploadService;
	
	@PostMapping(path = "/upload/upload", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> upload(UploadBean uploadBean, HttpSession session) {

		
		String config;
		try {
			User user = (User)session.getAttribute("User");
			UploadResult uploadResult = (UploadResult)session.getAttribute("UploadResult");
			
			uploadService.validateBasic(uploadBean, user);
			
			config = ScanConfigUtil.readConfig();
			
			ScanConfig scanConfig = ScanConfigUtil.parseHtml(config);
			
			logger.debug("scanConfig:" + scanConfig);
			
		} catch (IOException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
		}
		
		
		session.setAttribute("User", user);
		
		
		
		
		RestResult restResult = null;
		String json = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			loginService.testEbaoClient();
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("initialize success, json:" + json);
		
		} catch (EBaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			restResult = new RestResult("-1100",  e.getMessage());
			try {
				json = objectMapper.writeValueAsString(restResult);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.ok(json);
		
	}
	
	@PostMapping(path = "/login/loginEbao", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> loginEbao(@RequestBody LoginRequest loginRequest, HttpSession session) {

		
		String loginStatusJson = null;
		LoginStatus loginStatus = null;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			loginStatus = loginService.login(loginRequest);
			loginStatusJson = objectMapper.writeValueAsString(loginStatus);
			
			// write json to file
			Path path = Paths.get(loginStatusJsonFile);
			try ( Writer out = Files.newBufferedWriter(path, StandardCharsets.UTF_8) ) {
	        	out.write(loginStatusJson);
	        }
			
		} catch (StreamReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (EBaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.ok(loginStatusJson);
		
	}
	@PostMapping(path = "/login/testEbaoClient", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> testEbaoClient() {

		RestResult restResult = null;
		String json = null;
		ObjectMapper objectMapper = new ObjectMapper();
		try {
			loginService.testEbaoClient();
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("testEbaoClient success, json:" + json);
		
		} catch (EBaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			restResult = new RestResult("-1100",  e.getMessage());
			try {
				json = objectMapper.writeValueAsString(restResult);
			} catch (JsonProcessingException e1) {
				// TODO Auto-generated catch block
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.ok(json);
		
	}
}
