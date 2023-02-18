package com.tgl.newscan2rest.service;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.asprise.imaging.core.Imaging;
import com.tgl.newscan2rest.bean.ImageRecordSet;
import com.tgl.newscan2rest.bean.ScanConfig;
import com.tgl.newscan2rest.bean.ScanResult;
import com.tgl.newscan2rest.bean.TiffRecord;
import com.tgl.newscan2rest.bean.TiffRecords;
import com.tgl.newscan2rest.dto.ScanRequest;
import com.tgl.newscan2rest.util.ImageRecordHelper;
import com.tgl.newscan2rest.util.ScanConfigUtil;
import com.tgl.newscan2rest.util.ScanUtil;

import jakarta.xml.bind.JAXBException;

@Service
public class ScanService {
	static final Logger logger = LoggerFactory.getLogger(ScanService.class);
	static final String APP_ID = "TGL-Scan-2";

	static final String COLOR_MODE_BW = "blackwhite";
	static final String COLOR_MODE_COLOR = "color";
	static final String DUPLEX_MODE_SINGLE = "singleSided";
	static final String DUPLEX_MODE_DOUBLE = "doubleSided";

	public static final String RECORD_SET_FILE_FULL_NAME = System.getProperty("user.dir") + File.separator + "config" + File.separator + "imagerecordset.xml";

	
	//private static final double BLANK_PAGE_THRESHOLD = 0.000001d;

	//public static final String IMAGE_ARCHIVE_DIR = System.getProperty("user.dir") + File.separator + "image-archive";

	//public static final String SCAN_TEMP_DIR = IMAGE_ARCHIVE_DIR + File.separator + "temp";

	public ScanResult scan(ScanRequest scanRequest) {
		
		Imaging imaging = new Imaging(APP_ID, 0).setUseAspriseSourceSelectUI(false);
		
		String sourceName = scanRequest.getSourceName();
		String colorModeStr = scanRequest.getColorMode();
		String duplexModeStr = scanRequest.getDuplexMode();
		boolean queryFromPage = scanRequest.isQueryFromPage();
		int lastScanOrder = scanRequest.getLastScanOrder() == null ? 0 : Integer.valueOf(scanRequest.getLastScanOrder());
		
		ScanConfig scanConfig = null;
		ScanResult scanResult = null;
		try {
			String config = ScanConfigUtil.readConfig();
			scanConfig = ScanConfigUtil.parseHtml(config);
			
			long start = System.currentTimeMillis();
			
			ScanUtil.setLastScanOrder(lastScanOrder);
			
			List<TiffRecord> importedRecordList = ScanUtil.scan(imaging, sourceName, colorModeStr, duplexModeStr, queryFromPage, scanConfig);
			
			long end = System.currentTimeMillis();

		    logger.debug("The ScanUtil.scan took {} ms, size:{}", end - start, importedRecordList.size());
			
			//List<TiffRecord> importedRecordList = new ArrayList<>();
			for (TiffRecord tiff : importedRecordList) {
				logger.debug("tiff1.getFileName():" + tiff.getFileName());
			}
			
			// loadImageRecordSet
			ImageRecordSet recordSet = loadImageRecordSet();
			if (recordSet != null) {
				for (TiffRecord tiff : recordSet.getRecords().getRecordList()) {
					logger.debug("tiff2.getFileName():" + tiff.getFileName());
				}
			} else {
				logger.debug("loadImageRecordSet no record");
				recordSet = new ImageRecordSet();
				TiffRecords tiffRecords = new TiffRecords();
				tiffRecords.setRecordList(new ArrayList<TiffRecord>());
				recordSet.setRecords(tiffRecords);
			}
			
			// add new recordlist to existing recordset
			if (importedRecordList !=null && !importedRecordList.isEmpty()) {
				List<TiffRecord> tiffRecordList = recordSet.getRecords().getRecordList();
				for (TiffRecord tiff : importedRecordList) {
					tiffRecordList.add(tiff);
				}
			}
			
			
			// marshall to file
			TiffRecords tiffRecords = recordSet.getRecords();
			if (tiffRecords != null) {
				int recordSize = tiffRecords.getRecordList().size();
				logger.debug("tiff record size:" + recordSize);
			} else {
				logger.debug("tiffRecords is null");
			}
			
			ImageRecordHelper imageRecordHelper = ImageRecordHelper.getInstance();
			imageRecordHelper.marshalToFile(recordSet);
			
			scanResult = new ScanResult("0000", null);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			logger.error(e.getMessage(), e);
			
			scanResult = new ScanResult("-1000", e.getMessage());
		} catch (JAXBException e) {
			logger.error(e.getMessage(), e);
			String backupFileName = null;
			String errorMessage = "";
			errorMessage = String.format("%s 檔案內容有誤，無法解析！原檔案將備份並更名為 %s 以利除錯追蹤。", ImageRecordHelper.RECORD_SET_FILE_NAME, backupFileName);
			try {
				ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
				backupFileName = recordSetHelper.backupXmlFile();
			} catch (IOException ioe) {
    			logger.error("", ioe);
    			errorMessage = errorMessage + "\n" + ioe.getMessage();
			}
			scanResult = new ScanResult("-1000", errorMessage);
		}
		
		return scanResult;
	}
	private ImageRecordSet loadImageRecordSet() throws JAXBException {
		ImageRecordHelper recordSetHelper = ImageRecordHelper.getInstance();
		ImageRecordSet tmpRecordSet = null;
		if (recordSetHelper.xmlFileExists()) {
			tmpRecordSet = recordSetHelper.unmarshalFromFile();
		}
		return tmpRecordSet;
	}
	
}
