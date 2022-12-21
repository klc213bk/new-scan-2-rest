package com.tgl.newscan2rest.controller;

import java.io.File;
import java.io.IOException;
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
import com.tgl.newscan2rest.bean.ScanResult;
import com.tgl.newscan2rest.bean.SourcesResult;
import com.tgl.newscan2rest.dto.Greeting;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.dto.ScanRequest;
import com.tgl.newscan2rest.http.EBaoException;
import com.tgl.newscan2rest.service.LoginService;
import com.tgl.newscan2rest.service.ScanService;

@RestController
public class Controller {

	private static final String template = "Hello, %s!";
	private final AtomicLong counter = new AtomicLong();
	public static final String APP_ID = "TGL-Scan-2";
	public static final String IMAGE_ARCHIVE_DIR = System.getProperty("user.dir") + File.separator + "image-archive";
	public static final String SCAN_TEMP_DIR = IMAGE_ARCHIVE_DIR + File.separator + "temp";
	private static final double BLANK_PAGE_THRESHOLD = 0.000001d;

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

	@GetMapping("/scanConfig")
	public ResponseEntity<String> getScanConfig() {
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

	@PostMapping(path = "/scan", consumes = "application/json", produces = "application/json")
	public ResponseEntity<String> scan(@RequestBody ScanRequest scanRequest) {

		ScanResult scanResult = scanService.scan(scanRequest);

		ObjectMapper objectMapper = new ObjectMapper();
		String json = "";
		try {
//			Thread.sleep(2000);
			json = objectMapper.writeValueAsString(scanResult);
			logger.debug("json:" + json);
		} catch (JsonProcessingException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		System.out.println("resultJson:" + json);
		return ResponseEntity.ok(json);
	}

	@PostMapping(path = "/login", consumes = "application/json", produces = "application/json")
	public ResponseEntity<LoginStatus> login(@RequestBody LoginRequest loginRequest) {

		LoginStatus loginStatus = null;
		try {
			loginStatus = loginService.login(loginRequest);

//			ObjectMapper objectMapper = new ObjectMapper();
//			String json = "";
//			try {
//				json = objectMapper.writeValueAsString(loginStatus);
//				logger.debug("json:" + json);
//			} catch (JsonProcessingException e1) {
//				// TODO Auto-generated catch block
//				e1.printStackTrace();
//			}
//			
//			System.out.println("resultJson:" + json);

		} catch (EBaoException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ResponseEntity.ok(loginStatus);
	}

	@PostMapping(path = "/login2", consumes = "application/json", produces = "application/json")
	public ResponseEntity<LoginStatus> login2(@RequestBody LoginRequest loginRequest) {

		LoginStatus loginStatus = null;

		ObjectMapper objectMapper = new ObjectMapper();
		try {
			loginStatus = objectMapper.readValue(new File("loginstatus.json"), LoginStatus.class);
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

		return ResponseEntity.ok(loginStatus);
	}

//	@GetMapping("/scan2/{source}")
//	public ResponseEntity<String> scan2(@PathVariable String source) {
//	
//		ScanRequest scanRequest = new ScanRequest();
//		scanRequest.setColorMode("Black&White");
//		scanRequest.setDuplexMode("Dobule_Page_Scane");
//		scanRequest.setQueryFromPage(false);
//		scanRequest.setSourceName(source);
//		
//		String resultJson = scanService.scan2(scanRequest);
//		System.out.println("resultJson:" + resultJson);
//		return ResponseEntity.ok(resultJson);
//	}
//	@GetMapping("/settings")
//    public List<String> settings()
//    {
//    	List<String> sourceNameList = new ArrayList<>();
//    	String sourceName = null;
//    	
//    	Imaging imaging = new Imaging(APP_ID, 0);
//    	List<Source> sourcesNameOnly = imaging.scanListSources(true, "all", true, false);
//    	System.out.println("All sources with names only: \n" + sourcesNameOnly);
//    	for (Source source : sourcesNameOnly) {
//    		System.out.println("sourcesName:" + source.getSourceName());
//    		sourceNameList.add(source.getSourceName());
//    	}
//
//		return sourceNameList;
//    }
//    @GetMapping("/scan3/{source}")
//    public String scan3(@PathVariable String source) {
//
//    	String sourceName = source;//"PaperStream IP fi-7140";
//    	logger.debug("sourceName:" + sourceName);
//    	
//    	Imaging imaging = new Imaging(APP_ID, 0)
//			    .setUseAspriseSourceSelectUI(false);
//    	
//    	RequestOutputItem requestOutput = null;
//		int scanType = TwainConstants.TWPT_BW;
//		int scanDuplex = TwainConstants.TWDX_NONE;
//		boolean duplexEnabled = false;
//
////		if ( Constant.COLOR_MODE_BLACK_WHITE.equals(colorModeStr) ) {
//			requestOutput = new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_TIF);
//			requestOutput.setTiffCompression(Imaging.TIFF_COMPRESSION_CCITT_G4);
////		} else if ( Constant.COLOR_MODE_COLOR.equals(colorModeStr) ) {
////			scanType = TwainConstants.TWPT_RGB;
////			requestOutput = new RequestOutputItem(Imaging.OUTPUT_SAVE, Imaging.FORMAT_JPG);
////		}
//
//			requestOutput.setSavePath(SCAN_TEMP_DIR + "\\${TMS}${EXT}");
////			requestOutput.setSavePath("C:\\Users\\steven\\eclipse-workspace\\new-scan-2-rest\\new-scan-2-rest\\image-archive\\temp\\${TMS}${EXT}");
//		//requestOutput.setSavePath(SCAN_TEMP_DIR);
//		requestOutput.setForceSinglePage(true);
//		logger.debug("saved path:" + SCAN_TEMP_DIR);
//		
//		
////		if ( Constant.DUPLEX_MODE_SINGLE_PAGE.equals(duplexModeStr) ) {
//////			scanDuplex = TwainConstants.TWDX_1PASSDUPLEX;
////			scanDuplex = TwainConstants.TWDX_NONE;
////		} else if ( Constant.DUPLEX_MODE_DOUBLE_PAGE.equals(duplexModeStr) ) {
//			scanDuplex = TwainConstants.TWDX_2PASSDUPLEX;
//			duplexEnabled = true;
////		}
//
//		int stScanType = scanType;
//		int stScanDuplex = scanDuplex;
//		
//    	Map<String, String> algoMap = new HashMap<String, String>();
//		algoMap.put("type", "B"); // Types to try: DEFAULT, A, B, C 
//		
//    	Request request = new Request()
//    			.addOutputItem(requestOutput)
//    			.setRecognizeBarcodes(true)
//    			.setBarcodesSettings(algoMap)
////    			.setDetectBlankPages("false") 不偵測空白頁，由掃描器設定[空白頁檢測]控制
//    			.setBlankPageThreshold(BLANK_PAGE_THRESHOLD) // 因部份條碼貼紙或自行列印的條碼辨識不到，故由 0.02 降低至 0.000001 (與舊版掃描設定相同)
//    			.setTwainCap(TwainConstants.ICAP_PIXELTYPE, stScanType)
//    			.setTwainCap(TwainConstants.CAP_DUPLEXENABLED, duplexEnabled)
//    			.setTwainCap(TwainConstants.CAP_DUPLEX, scanDuplex)
//    			;
//    	
//    	if (logger.isDebugEnabled()) {
//			retrieveExtAttributes(request);
//		}
//    	
//    	if (logger.isDebugEnabled()) {
//			logger.debug("imaging.scan()");
//			logger.debug("request = {}", request.toJson(true));
//		}
//
//		// 開始掃描
//		com.asprise.imaging.core.Result result = imaging.scan(request, sourceName, false, true);
//
//		String resultJson = result.toJson(true);
//		if (logger.isDebugEnabled()) {
//			logger.debug("result = {}", result == null ? "(null)" : resultJson);
//		}
//		
//		List<File> acquireFiles = result.getImageFiles();
//		if ( acquireFiles==null || acquireFiles.size()==0 ) {
//			if (logger.isDebugEnabled()) {
//				logger.debug("no acquireFiles....");
//			}
//			return null;
//		}
//		for ( int i=0; i<acquireFiles.size(); i++ ) {
//	    	File acquireFile = acquireFiles.get(i);
//			if (logger.isDebugEnabled()) {
//				logger.debug("Acquire image :{}", acquireFile.getName());
//			}
//		}
//		return resultJson;
//    }
//    private static void retrieveExtAttributes(Request request) {
//		int[] capAttrs = { 
//			TwainConstants.ICAP_BARCODEDETECTIONENABLED, 
//			TwainConstants.ICAP_SUPPORTEDBARCODETYPES, 
//			TwainConstants.ICAP_BARCODEMAXSEARCHPRIORITIES, 
//			TwainConstants.ICAP_BARCODESEARCHPRIORITIES, 
//			TwainConstants.ICAP_BARCODESEARCHMODE, 
//			TwainConstants.ICAP_BARCODEMAXRETRIES, 
//			TwainConstants.ICAP_BARCODETIMEOUT
//		};
//		String[] capAttrNames = { 
//			"ICAP_BARCODEDETECTIONENABLED", 
//			"ICAP_SUPPORTEDBARCODETYPES", 
//			"ICAP_BARCODEMAXSEARCHPRIORITIES", 
//			"ICAP_BARCODESEARCHPRIORITIES", 
//			"ICAP_BARCODESEARCHMODE", 
//			"ICAP_BARCODEMAXRETRIES", 
//			"ICAP_BARCODETIMEOUT"
//		};
//		int[] extAttrs = { 
//			TwainConstants.TWEI_BARCODEX, 
//			TwainConstants.TWEI_BARCODEY, 
//			TwainConstants.TWEI_BARCODETEXT, 
//			TwainConstants.TWEI_BARCODETYPE, 
//			TwainConstants.TWEI_BARCODECOUNT, 
//			TwainConstants.TWEI_BARCODECONFIDENCE, 
//			TwainConstants.TWEI_BARCODEROTATION, 
//			TwainConstants.TWEI_BARCODETEXTLENGTH
//		};
//		String[] extAttrNames = { 
//			"TWEI_BARCODEX", 
//			"TWEI_BARCODEY", 
//			"TWEI_BARCODETEXT", 
//			"TWEI_BARCODETYPE", 
//			"TWEI_BARCODECOUNT", 
//			"TWEI_BARCODECONFIDENCE", 
//			"TWEI_BARCODEROTATION", 
//			"TWEI_BARCODETEXTLENGTH"
//		};
//
//		for (int i=0; (i<capAttrs.length && i<=160); i++) {
//			int attr = capAttrs[i];
//			String attrName = capAttrNames[i];
//			logger.debug("{}={}", attrName, attr);
//			request.retrieveCap(attr);
//		}
//		for (int j=0; j<extAttrs.length; j++) {
//			int attr = extAttrs[j];
//			String attrName = extAttrNames[j];
//			logger.debug("{}={}", attrName, attr);
//			request.retrieveExtendedImageAttributes(attr);
//		}
//
//	}
}
