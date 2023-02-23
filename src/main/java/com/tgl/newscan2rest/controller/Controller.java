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
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tgl.newscan2rest.bean.BatchDeleteBean;
import com.tgl.newscan2rest.bean.CopyRecord;
import com.tgl.newscan2rest.bean.DeleteRecord;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.ImportRecord;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.PageWarning;
import com.tgl.newscan2rest.bean.RestResult;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScanConfigResult;
import com.tgl.newscan2rest.bean.ScanResult;
import com.tgl.newscan2rest.bean.SourcesResult;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.bean.UpdateRecordBean;
import com.tgl.newscan2rest.bean.UploadBean;
import com.tgl.newscan2rest.dto.Greeting;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.dto.ScanRequest;
import com.tgl.newscan2rest.http.EBaoException;
import com.tgl.newscan2rest.service.CopyService;
import com.tgl.newscan2rest.service.DeleteService;
import com.tgl.newscan2rest.service.ImportService;
import com.tgl.newscan2rest.service.LoginService;
import com.tgl.newscan2rest.service.ScanService;
import com.tgl.newscan2rest.service.UpdateService;
import com.tgl.newscan2rest.service.UploadService;
import com.tgl.newscan2rest.util.ScanConfigUtil;
import com.tgl.newscan2rest.util.ScanUtil;

import net.asprise.commons.lang3.builder.ToStringBuilder;

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
	
	@Autowired
	DeleteService deleteService;
	
	@Autowired
	CopyService copyService;
	
	@Autowired
	ImportService importService;
	
	@Autowired
	UpdateService updateService;
	
	@Autowired
	UploadService uploadService;

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
		
		
			String htmlBody = "";
			try {
				htmlBody = ScanConfigUtil.readConfig();
				
				logger.debug("after readConfig...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			ScanConfig scanConfig = null;
			try {
				scanConfig = ScanConfigUtil.parseHtml(htmlBody);
				logger.debug("aa" +  scanConfig.getMainFileTypeList());
				logger.debug("bb" +  scanConfig.getOrgName());
				logger.debug("cc:" +  scanConfig.getDeptName());
				logger.debug("after parseHtml...");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			String importFilePath = "C:\\Users\\steven\\Flutter\\tgl\\bloc\\projects\\sandbox22\\image-archive\\2023011317574865301_01028074972.tiff";
			File selectedFile = new File(importFilePath);
			logger.debug("dd:" +  selectedFile);
			ScanUtil.setLastScanOrder(1);
			ScanUtil.setMultiPolicy(false);
			ScanUtil.setFileCodeCount(0);
			List<TiffRecord> recordList = ScanUtil.convertFile(selectedFile, false, scanConfig);
			logger.debug("recordList size:" + recordList.size());
		
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
	
	@PostMapping(path = "/importRecord", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> importRecord(@RequestBody ImportRecord importRecord) {
		logger.debug("Controller importRecord is called");
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			importService.importRecord(importRecord);
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("importRecord success, json:" + json);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("Controller importRecord error:" + e.getMessage());
			
			restResult = new RestResult("-1000", e.getMessage());
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller importRecord JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		logger.debug("Controller importRecord is complete");
		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/copy", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> copy(@RequestBody CopyRecord copyRecord) {
		logger.debug("Controller copy is called");
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			
			copyService.copy(copyRecord);
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("copy success, json:" + json);
		} catch (Exception e) {
			logger.error("Controller copy error:" + e.getMessage());
			
			restResult = new RestResult("-1000", e.getMessage());
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller copy JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		logger.debug("Controller copy is complete");
		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/upload", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> upload() {
		logger.debug("Controller upload is called");
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			uploadService.uploadAll();;
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("upload success, json:" + json);
		} catch (Exception e) {
			restResult = new RestResult("-1000", e.getMessage());
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller uploadAll JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		
		logger.debug("Controller upload is complete");
		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/uploadSaveTiffData", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> uploadSaveTiffData(@RequestBody UploadBean uploadBean) {
		logger.debug("Controller uploadSaveTiffData is called");
		logger.debug("uploadSaveTiffData:" + ToStringBuilder.reflectionToString(uploadBean));
		
		int selectedIndex = uploadBean.getSelectedIndex();
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			
//			uploadService.uploadSaveTiffData(selectedIndex);
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("uploadSaveTiffData success , json:" + json);
		} catch (Exception e) {
			restResult = new RestResult("-1000", e.getMessage());
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller uploadSaveTiffData JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		
		logger.debug("Controller uploadSaveTiffData is complete");
		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/updateRecord", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> updateRecord(@RequestBody UpdateRecordBean updateRecordBean) {
		logger.debug("Controller updateRecord is called");
		logger.debug("updateRecordBean:" + ToStringBuilder.reflectionToString(updateRecordBean));
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			updateService.updateRecord(updateRecordBean);
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("updateRecord success, json:" + json);
		} catch (Exception e) {
			restResult = new RestResult("-1000", e.getMessage());
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller updateRecord JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		
		logger.debug("Controller updateRecord is complete");
		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/delete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> delete(@RequestBody DeleteRecord deleteRecord) {
		logger.debug("Controller delete is called");
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			
			deleteService.delete(deleteRecord);
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
		} catch (Exception e) {
			
			restResult = new RestResult("-1000", e.getMessage());
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller delete JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		logger.debug("Controller delete is complete");
		return ResponseEntity.ok(json);
	}
	@PostMapping(path = "/batchDelete", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
	public ResponseEntity<Object> batchDelete(@RequestBody BatchDeleteBean batchDeleteBean) {
		logger.debug("Controller batchDelete is called");
		logger.debug("BatchDeleteBean :" + batchDeleteBean.getFromScanOrder() +", " + batchDeleteBean.getToScanOrder());
		
		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		RestResult restResult = null;
		
		try {
			deleteService.batchDelete(batchDeleteBean);
			
			restResult = new RestResult("0000", null);
			
			json = objectMapper.writeValueAsString(restResult);
			
			logger.debug("batchDelete success, json:" + json);
		} catch (Exception e) {
			restResult = new RestResult("-1000", "kkkkksee logs for details");
			
			try {
				json = objectMapper.writeValueAsString(restResult);
				
			} catch (JsonProcessingException e1) {
				e1.printStackTrace();
				logger.error("Controller batchDelete JsonProcessingException error:" + e1.getMessage());
				json = "{\"errorCode\":\"-1100\",\"errorMessage\":\"see logs for details\"}";
			}	
			
		}
		
		logger.debug("Controller batchDelete is complete");
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
			
			scanResult = new ScanResult("0000", null);
			json = objectMapper.writeValueAsString(scanResult);
			//json = "{\"errorCode\":\"0000\",\"errorMessage\":null}";
			
			logger.debug("json222:" + json);
		
		} catch (JsonProcessingException e1) {
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
