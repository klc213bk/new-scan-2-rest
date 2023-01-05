package com.tgl.newscan2rest.controller;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.asprise.imaging.core.Imaging;
import com.asprise.imaging.core.Request;
import com.asprise.imaging.core.RequestOutputItem;
import com.asprise.imaging.core.scan.twain.Source;
import com.asprise.imaging.core.scan.twain.TwainConstants;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScanConfigResult;
import com.tgl.newscan2rest.bean.ScanResult;
import com.tgl.newscan2rest.bean.SourcesResult;
import com.tgl.newscan2rest.dto.Greeting;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.dto.ScanRequest;
import com.tgl.newscan2rest.http.EBaoException;
import com.tgl.newscan2rest.service.LoginService;
import com.tgl.newscan2rest.service.ScanService;
import com.tgl.newscan2rest.util.ScanConfigUtil;

@RestController
public class Controller {
	private static final String CONFIG_DIR = System.getProperty("user.dir") + File.separator + "config";
	
	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	public static final String APP_ID = "TGL-Scan-2";
	public static final String IMAGE_ARCHIVE_DIR = System.getProperty("user.dir") + File.separator + "image-archive";
	public static final String SCAN_TEMP_DIR = IMAGE_ARCHIVE_DIR + File.separator + "temp";
	private static final double BLANK_PAGE_THRESHOLD = 0.000001d;
	
	private static final String loginStatusJsonFile = CONFIG_DIR + File.separator + "loginStatus.json";
	//private static final String loginConfigJsonFile = CONFIG_DIR + File.separator + "loginConfig.json";
	
	private static final Logger logger = LoggerFactory.getLogger(Controller.class);

	@Autowired
	ScanService scanService;

	@Autowired
	LoginService loginService;

	@GetMapping("/greeting/{name}")
	public Greeting greeting(@PathVariable String name) {
		return new Greeting(counter.incrementAndGet(), String.format(template, name));
	}

	@GetMapping("/sources")
	public ResponseEntity<String> sources() {
		List<String> sourceNameList = new ArrayList<>();

		Imaging imaging = new Imaging("TGL-Scan-2", 0);
		List<Source> sourcesNameOnly = imaging.scanListSources(true, "all", true, false);
		sourcesNameOnly.forEach(e -> sourceNameList.add(e.getSourceName()));

		SourcesResult sourcesResult = new SourcesResult();
		sourcesResult.setSourceList(sourceNameList);

		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		try {
			json = objectMapper.writeValueAsString(sourcesResult);
			logger.debug("json:" + json);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return ResponseEntity.ok(json);

	}

	@GetMapping("/testStr")
	public ResponseEntity<String> testStr() {
		String json = "{\"rating\":\"4.5\",\"img\":\"lib/utils/assets/image_1\"}";
		System.out.println("json" + json);
		return ResponseEntity.ok(json);
	}
	@GetMapping("/loginStatus")
	public ResponseEntity<String> getLoginStatus() {
		
		Path path = Paths.get(loginStatusJsonFile);
		StringBuffer sb = new StringBuffer();
		try {
			for (String line : Files.readAllLines(path)) {
				sb.append(line);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		String json = sb.toString();
		
		return ResponseEntity.ok(json);
		
	}

	@PostMapping(path = "/scan", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> scan(@RequestBody ScanRequest scanRequest) {

		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		ScanResult scanResult = null;
		try {
			scanResult = scanService.scan(scanRequest);

			json = objectMapper.writeValueAsString(scanResult);
			
//			scanResult = new ScanResult("0000", null);
//			json = objectMapper.writeValueAsString(scanResult);
			//json = "{\"errorCode\":\"0000\",\"errorMessage\":null}";

			logger.debug("json:" + json);
		
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/scan2", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> scan2(@RequestBody ScanRequest scanRequest) {

		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		ScanResult scanResult = null;
		try {
			scanResult = scanService.scan(scanRequest);

			json = objectMapper.writeValueAsString(scanResult);
			
//			scanResult = new ScanResult("0000", null);
//			json = objectMapper.writeValueAsString(scanResult);
			//json = "{\"errorCode\":\"0000\",\"errorMessage\":null}";
			Thread.sleep(2000);
			
			logger.debug("json222:" + json);
		
		} catch (JsonProcessingException | InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return ResponseEntity.ok(json);
	}
	@GetMapping("/scan3")
	public ResponseEntity<String> scan3() {
		
		ScanRequest scanRequest = new ScanRequest();
		scanRequest.setColorMode("Black&White");
		scanRequest.setDuplexMode("Dobule_Page_Scane");
		scanRequest.setQueryFromPage(false);
		scanRequest.setSourceName("PaperStream IP fi-7140");
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		try {
			ScanResult scanResult = scanService.scan(scanRequest);
			
			json = objectMapper.writeValueAsString(scanResult);
			logger.debug("json3:" + json);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		return ResponseEntity.ok(json);

	}
	@PostMapping(path = "/logoutEbao", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> logoutEbao() {
		String loginStatusJson = null;
		LoginStatus loginStatus = null;
		
		ObjectMapper objectMapper = new ObjectMapper();
		loginStatus = loginService.logout();
		try {
			loginStatusJson = objectMapper.writeValueAsString(loginStatus);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return ResponseEntity.ok(loginStatusJson);
			
	}
	@PostMapping(path = "/loginEbao", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> loginEbao(@RequestBody LoginRequest loginRequest) {

		
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
		
		
////		ObjectMapper objectMapper = new ObjectMapper();
//		LoginStatus loginStatus = null;
//		String loginStatusJson = "";
//		try {
////			loginStatus = loginService.login(loginRequest);
////				
////			json = objectMapper.writeValueAsString(loginStatus);
//			
//			
//			Path path = Paths.get(loginStatusJsonFile);
//			StringBuffer sb = new StringBuffer();
//			for (String line : Files.readAllLines(path)) {
//				sb.append(line);
//			}
//			loginStatusJson = sb.toString();
//			
//			
//			// write json to file
////			Path path = Paths.get(loginStatusJsonFile2);
////		    byte[] strToBytes = json.getBytes();
////
////		    Files.write(path, strToBytes);
//			logger.debug("json:" + loginStatusJson);
//			
//
//		} catch (/*EBaoException |*/ IOException e) {
//			e.printStackTrace();
//			loginStatus.setProcessCode(LoginStatus.PROC_CODE_SYSTEM_ERROR);
//			
//		}
//		return ResponseEntity.ok(loginStatusJson);
	}

	@PostMapping(path = "/loginEbao2", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> loginEbao2(@RequestBody LoginRequest loginRequest) {

		String loginStatusJson = null;

		try {
			Path path = Paths.get(loginStatusJsonFile);
			StringBuffer sb = new StringBuffer();
			for (String line : Files.readAllLines(path)) {
				sb.append(line);
			}
			loginStatusJson = sb.toString();
			
		} catch (StreamReadException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (DatabindException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 
		return ResponseEntity.ok(loginStatusJson);
	}

}
