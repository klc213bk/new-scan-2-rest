package com.tgl.newscan2rest.service;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.ListIterator;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import com.tgl.newscan2rest.Constant;
import com.tgl.newscan2rest.bean.BatchDeleteBean;
import com.tgl.newscan2rest.bean.CopyRecord;
import com.tgl.newscan2rest.bean.DeleteRecord;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.ImportRecord;
import com.tgl.newscan2rest.bean.LoginStatus;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScannedImage;
import com.tgl.newscan2rest.bean.TiffField;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.dto.LoginRequest;
import com.tgl.newscan2rest.util.ImageRecordHelper;
import com.tgl.newscan2rest.util.ImageUtil;
import com.tgl.newscan2rest.util.ScanConfigUtil;
import com.tgl.newscan2rest.util.ScanUtil;
import com.tgl.newscan2rest.util.TiffRecordUtil;

import jakarta.xml.bind.JAXBException;

@Service
public class ImportService {

	private static final Logger logger = LogManager.getLogger(ImportService.class);

	public void importRecord(ImportRecord importRecord) throws Exception {
		
		boolean isQueryFromPage = importRecord.isQueryFromPage();
		String importFilePath = importRecord.getImportFilePath();
		logger.debug("isQueryFromPage:" +isQueryFromPage);
		logger.debug("importFilePath:" +importFilePath);
		
		String errorMessage = null;
		
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet recordSet = recordSetHelper.unmarshalFromFile();
		List<TiffRecord> recordList = recordSet.getRecords().getRecordList();
		
		int lastIndex = recordList.size() - 1;
		TiffRecord lastTiffRecord = recordList.get(lastIndex);
		ScannedImage lastScannedImage = recordSetHelper.convert(lastTiffRecord);
		int lastScanOrder = Integer.valueOf(lastScannedImage.scanOrderProperty().getValue());
		
		File selectedFile = new File(importFilePath);

		String htmlBody = "";
		ScanConfig scanConfig = null;
		
		try {
			htmlBody = ScanConfigUtil.readConfig();
			
			logger.debug("after readConfig...");
			
			scanConfig = ScanConfigUtil.parseHtml(htmlBody);
			logger.debug("after parseHtml...");
			
		} catch (Exception e) {
			e.printStackTrace();
			errorMessage = String.format("無法解析scanconfig.xml %s ！", e.getMessage());
		}
		
		if (errorMessage == null) {
			ScanUtil.setLastScanOrder(lastScanOrder);
			ScanUtil.setMultiPolicy(false);
			ScanUtil.setFileCodeCount(0);
			List<TiffRecord> importedRecordList = ScanUtil.convertFile(selectedFile, isQueryFromPage, scanConfig);
			if (logger.isDebugEnabled()) {
				logger.debug("Image imported! size: " + importedRecordList.size());
			}
		// =====
			if (importedRecordList != null) {
				
				for (TiffRecord newTiffRecord : importedRecordList) {
					recordList.add(newTiffRecord);
				}
			
				try {
					recordSetHelper.marshalToFile(recordSet);
	    		} catch (JAXBException e) {
					errorMessage = String.format("無法儲存設定檔 %s ！", ImageRecordHelper.RECORD_SET_FILE_NAME);
					logger.error(errorMessage, e);
				}
			}
		}
		if (null != errorMessage) {
			throw new Exception(errorMessage);
		}
	}
	
}


